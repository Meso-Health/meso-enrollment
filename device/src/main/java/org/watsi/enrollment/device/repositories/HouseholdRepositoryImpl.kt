package org.watsi.enrollment.device.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.enrollment.device.api.CoverageApi
import org.watsi.enrollment.device.api.SyncHouseholdApi
import org.watsi.enrollment.device.db.daos.DeltaDao
import org.watsi.enrollment.device.db.daos.HouseholdDao
import org.watsi.enrollment.device.db.daos.MemberDao
import org.watsi.enrollment.device.db.models.DeltaModel
import org.watsi.enrollment.device.db.models.HouseholdEnrollmentRecordModel
import org.watsi.enrollment.device.db.models.HouseholdModel
import org.watsi.enrollment.device.db.models.MemberEnrollmentRecordModel
import org.watsi.enrollment.device.db.models.MemberModel
import org.watsi.enrollment.device.db.models.MembershipPaymentModel
import org.watsi.enrollment.device.managers.PreferencesManager
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.Household
import org.watsi.enrollment.domain.relations.HouseholdWithExtras
import org.watsi.enrollment.domain.relations.HouseholdWithMembers
import org.watsi.enrollment.domain.relations.HouseholdWithMembersAndPayments
import org.watsi.enrollment.domain.repositories.HouseholdRepository
import java.util.UUID

class HouseholdRepositoryImpl(
        private val householdDao: HouseholdDao,
        private val deltaDao: DeltaDao,
        private val memberDao: MemberDao,
        private val api: CoverageApi,
        private val sessionManager: SessionManager,
        private val clock: Clock,
        private val okHttpClient: OkHttpClient,
        private val preferencesManager: PreferencesManager
) : HouseholdRepository {
    override fun get(householdId: UUID): Flowable<HouseholdWithMembersAndPayments> {
        return householdDao.getFlowable(householdId).map {
            it.toHouseholdWithMembersAndPayments()
        }
    }

    override fun createdOrEditedAfter(instant: Instant): Flowable<List<HouseholdWithMembers>> {
        return householdDao.createdOrEditedAfter(instant).map { it.map { it.toHouseholdWithMembers() } }
    }

    override fun save(household: Household): Completable {
        return Completable.fromAction {
            // TODO: Create abstraction for creating these DeltaModels
            householdDao.insertWithDelta(
                HouseholdModel.fromHousehold(household, clock),
                DeltaModel(
                    action = Delta.Action.ADD,
                    modelName = Delta.ModelName.HOUSEHOLD,
                    modelId = household.id,
                    synced = false,
                    createdAt = Instant.now(clock),
                    updatedAt = Instant.now(clock),
                    field = null)
            )
        }.subscribeOn(Schedulers.io())
    }

    override fun fetch(): Completable {
        return sessionManager.currentAuthenticationToken()?.let { token ->
            Completable.fromAction {
                // This first check is to make sure we don't overwrite any members who've been
                // recently edited before the server request is made (since there's a strong possibility
                // they won't sync in time and we don't want them to be overwritten)
                val unsyncedMemberIds = deltaDao.unsyncedModelIds(Delta.ModelName.MEMBER, Delta.Action.EDIT).blockingGet()
                val householdsFromServer = api.fetchHouseholds(token.getHeaderString()).blockingGet()
                val clientMembers = memberDao.all().blockingFirst()
                val clientMembersById = clientMembers.groupBy { it.id }

                // Convert server data to entities
                val householdsWithExtras = householdsFromServer.map {
                    HouseholdWithExtras(
                        household = Household(
                            id = it.id,
                            enrolledAt = it.enrolledAt,
                            administrativeDivisionId = it.administrativeDivisionId,
                            address = it.address
                        ),
                        activeHouseholdEnrollmentRecord = it.activeHouseholdEnrollmentRecord?.toHouseholdEnrollmentRecord(),
                        members = it.members.map { memberApi ->
                            val persistedMember = clientMembersById[memberApi.id]?.firstOrNull()?.toMember()
                            memberApi.toMember(persistedMember)
                        },
                        memberEnrollmentRecords = it.memberEnrollmentRecords.map { it.toMemberEnrollmentRecord() },
                        membershipPayments = it.activeMembershipPayments.map { it.toMembershipPayment() }
                    )
                }

                // Convert entities to models
                val householdModels = householdsWithExtras.map {
                    HouseholdModel.fromHousehold(it.household, clock)
                }

                // This second check is to make sure we check for any new member edits made during server request
                val additionalUnsyncedMemberIds = deltaDao.unsyncedModelIds(Delta.ModelName.MEMBER, Delta.Action.EDIT).blockingGet()

                val memberModels = householdsWithExtras.map {
                    it.members.map { member ->
                        MemberModel.fromMember(member, clock)
                    }
                }.flatten().filterNot {
                    unsyncedMemberIds.contains(it.id) ||
                    additionalUnsyncedMemberIds.contains(it.id)
                }

                val householdEnrollmentRecordModels = householdsWithExtras.mapNotNull {
                    it.activeHouseholdEnrollmentRecord?.let { householdEnrollmentRecord ->
                        HouseholdEnrollmentRecordModel.fromHouseholdEnrollmentRecord(householdEnrollmentRecord, clock)
                    }
                }

                val memberEnrollmentRecordModels = householdsWithExtras.map {
                    it.memberEnrollmentRecords.map { memberEnrollmentRecord ->
                        MemberEnrollmentRecordModel.fromMemberEnrollmentRecord(memberEnrollmentRecord, clock)
                    }
                }.flatten()

                val paymentModels = householdsWithExtras.map {
                    it.membershipPayments.map { membershipPayment ->
                        MembershipPaymentModel.fromMembershipPayment(membershipPayment, clock)
                    }
                }.flatten()

                // Save models
                householdDao.upsert(
                    householdModels = householdModels,
                    memberModels = memberModels,
                    householdEnrollmentRecordModels = householdEnrollmentRecordModels,
                    memberEnrollmentRecordModels = memberEnrollmentRecordModels,
                    paymentModels = paymentModels
                )
                preferencesManager.updateHouseholdsLastFetched(clock.instant())
            }.subscribeOn(Schedulers.io())
        } ?: Completable.error(Exception("Current token is null while calling HouseholdRepositoryImpl.fetch"))
    }

    override fun sync(deltas: List<Delta>): Completable {
        return sessionManager.currentAuthenticationToken()?.let { authToken ->
            householdDao.get(deltas.first().modelId).flatMap {
                val household = it.toHousehold()

                if (deltas.any { it.action == Delta.Action.ADD }) {
                    api.postHousehold(authToken.getHeaderString(), SyncHouseholdApi(household))
                } else {
                    Single.error(
                        IllegalStateException("Deltas with actions ${deltas.map { it.action }} not supported for Household")
                    )
                }
            }.toCompletable().subscribeOn(Schedulers.io())
        } ?: Completable.error(Exception("Current token is null while calling HouseholdRepositoryImpl.sync"))
    }

    override fun deleteAll(): Completable {
        return Completable.fromAction {
            okHttpClient.cache()?.evictAll()
            householdDao.deleteAll()
        }.subscribeOn(Schedulers.io())
    }
}

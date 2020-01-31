package org.watsi.enrollment.device.repositories

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.threeten.bp.Clock
import org.watsi.enrollment.device.api.CoverageApi
import org.watsi.enrollment.device.api.HouseholdEnrollmentRecordApi
import org.watsi.enrollment.device.db.daos.HouseholdEnrollmentRecordDao
import org.watsi.enrollment.device.db.models.DeltaModel
import org.watsi.enrollment.device.db.models.HouseholdEnrollmentRecordModel
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.HouseholdEnrollmentRecord
import org.watsi.enrollment.domain.repositories.HouseholdEnrollmentRecordRepository

class HouseholdEnrollmentRecordRepositoryImpl constructor(
    private val householdEnrollmentRecordDao: HouseholdEnrollmentRecordDao,
    private val api: CoverageApi,
    private val clock: Clock,
    private val sessionManager: SessionManager,
    private val okHttpClient: OkHttpClient
) : HouseholdEnrollmentRecordRepository {

    override fun save(householdEnrollmentRecord: HouseholdEnrollmentRecord, delta: Delta): Completable {
        return Completable.fromAction {
            householdEnrollmentRecordDao.insert(
                HouseholdEnrollmentRecordModel.fromHouseholdEnrollmentRecord(householdEnrollmentRecord, clock),
                DeltaModel.fromDelta(delta, clock)
            )
        }.subscribeOn(Schedulers.io())
    }

    override fun sync(deltas: List<Delta>): Completable {
        return sessionManager.currentAuthenticationToken()?.let { authToken ->
            householdEnrollmentRecordDao.get(deltas.first().modelId).flatMap {
                val householdEnrollmentRecord = it.toHouseholdEnrollmentRecord()
                if (deltas.any { it.action == Delta.Action.ADD }) {
                    api.postHouseholdEnrollmentRecord(
                        authorization = authToken.getHeaderString(),
                        householdEnrollmentRecord = HouseholdEnrollmentRecordApi(householdEnrollmentRecord)
                    )
                } else {
                    throw IllegalStateException("Deltas with ${deltas.map { it.action } } actions not supported for HouseholdEnrollmentRecord")
                }
            }.toCompletable().subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }

    override fun deleteAll(): Completable {
        return Completable.fromAction {
            okHttpClient.cache()?.evictAll()
            householdEnrollmentRecordDao.deleteAll()
        }.subscribeOn(Schedulers.io())
    }
}

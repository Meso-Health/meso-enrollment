package org.watsi.enrollment.device.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.threeten.bp.Clock
import org.watsi.enrollment.device.db.daos.DeltaDao
import org.watsi.enrollment.device.db.models.DeltaModel
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.repositories.DeltaRepository

class DeltaRepositoryImpl(
    private val deltaDao: DeltaDao,
    private val clock: Clock,
    private val okHttpClient: OkHttpClient
) : DeltaRepository {

    override fun unsynced(modelName: Delta.ModelName): Single<List<Delta>> {
        return deltaDao.unsynced(modelName).map { deltaModels ->
            deltaModels.map { it.toDelta() }
        }.subscribeOn(Schedulers.io())
    }

    override fun markAsSynced(deltas: List<Delta>): Completable {
        return Completable.fromAction {
            deltaDao.update(deltas.map { DeltaModel.fromDelta(it.copy(synced = true), clock) })
        }.subscribeOn(Schedulers.io())
    }

    override fun syncStatus(): Flowable<DeltaRepository.SyncStatus> {
        val syncFlowables = listOf(
            deltaDao.countUnsynced(Delta.ModelName.HOUSEHOLD),
            deltaDao.countUnsynced(Delta.ModelName.HOUSEHOLD_ENROLLMENT_RECORD),
            deltaDao.countUnsynced(Delta.ModelName.MEMBER),
            deltaDao.countUnsynced(Delta.ModelName.MEMBER_ENROLLMENT_RECORD),
            deltaDao.countUnsynced(Delta.ModelName.PHOTO),
            deltaDao.countUnsynced(Delta.ModelName.MEMBERSHIP_PAYMENT)
        )
        return Flowable.combineLatest(syncFlowables) { results ->
            DeltaRepository.SyncStatus(
                unsyncedHouseholdsCount = results[0] as Int,
                unsyncedHouseholdEnrollmentRecordsCount = results[1] as Int,
                unsyncedMembersCount = results[2] as Int,
                unsyncedMemberEnrollmentRecordsCount = results[3] as Int,
                unsyncedPhotosCount = results[4] as Int,
                unsyncedMembershipPaymentsCount = results[5] as Int
            )
        }
    }

    override fun deleteAll(): Completable {
        return Completable.fromAction {
            okHttpClient.cache()?.evictAll()
            deltaDao.deleteAll()
        }.subscribeOn(Schedulers.io())
    }
}

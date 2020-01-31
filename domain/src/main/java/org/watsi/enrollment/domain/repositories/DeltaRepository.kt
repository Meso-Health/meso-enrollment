package org.watsi.enrollment.domain.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.watsi.enrollment.domain.entities.Delta

interface DeltaRepository {
    fun unsynced(modelName: Delta.ModelName): Single<List<Delta>>
    fun markAsSynced(deltas: List<Delta>): Completable
    fun syncStatus(): Flowable<SyncStatus>
    fun deleteAll(): Completable

    data class SyncStatus(val unsyncedHouseholdsCount: Int,
                          val unsyncedHouseholdEnrollmentRecordsCount: Int,
                          val unsyncedMembersCount: Int,
                          val unsyncedMemberEnrollmentRecordsCount: Int,
                          val unsyncedPhotosCount: Int,
                          val unsyncedMembershipPaymentsCount: Int)
}

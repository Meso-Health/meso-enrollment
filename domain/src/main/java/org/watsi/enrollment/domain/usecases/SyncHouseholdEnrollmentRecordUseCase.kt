package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.repositories.DeltaRepository
import org.watsi.enrollment.domain.repositories.HouseholdEnrollmentRecordRepository

class SyncHouseholdEnrollmentRecordUseCase(
    private val householdEnrollmentRecordRepository: HouseholdEnrollmentRecordRepository,
    private val deltaRepository: DeltaRepository
) {
    fun execute(onError: (throwable: Throwable) -> Boolean): Completable {
        return Completable.fromAction {
            val unsyncedHouseholdDetlas = deltaRepository.unsynced(Delta.ModelName.HOUSEHOLD_ENROLLMENT_RECORD).blockingGet()
            unsyncedHouseholdDetlas.groupBy { it.modelId }.values.forEach { deltas ->
                Completable.fromAction {
                    householdEnrollmentRecordRepository.sync(deltas).blockingAwait()
                    deltaRepository.markAsSynced(deltas).blockingAwait()
                }.onErrorComplete {
                    onError(it.cause ?: it)
                }.blockingAwait()
            }
        }.subscribeOn(Schedulers.io())
    }
}

package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.repositories.DeltaRepository
import org.watsi.enrollment.domain.repositories.HouseholdRepository

class SyncHouseholdUseCase(
        private val householdRepository: HouseholdRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(onError: (throwable: Throwable) -> Boolean): Completable {
        return Completable.fromAction {
            val unsyncedHouseholdDetlas = deltaRepository.unsynced(Delta.ModelName.HOUSEHOLD).blockingGet()
            unsyncedHouseholdDetlas.groupBy { it.modelId }.values.forEach { householdDeltas ->
                Completable.fromAction {
                    householdRepository.sync(householdDeltas).blockingAwait()
                    deltaRepository.markAsSynced(householdDeltas).blockingAwait()
                }.onErrorComplete {
                    onError(it.cause ?: it)
                }.blockingAwait()
            }
        }.subscribeOn(Schedulers.io())
    }
}

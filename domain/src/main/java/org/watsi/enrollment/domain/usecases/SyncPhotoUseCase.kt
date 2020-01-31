package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.repositories.DeltaRepository
import org.watsi.enrollment.domain.repositories.PhotoRepository

class SyncPhotoUseCase(
        private val photoRepository: PhotoRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(onError: (throwable: Throwable) -> Boolean): Completable {
        return Completable.fromAction {
            val unsyncedMemberIds = deltaRepository.unsynced(Delta.ModelName.MEMBER).blockingGet()
                    .filter { it.action == Delta.Action.ADD }
                    .map { it.modelId }
                    .distinct()
            val unsyncedPhotoDeltas = deltaRepository.unsynced(Delta.ModelName.PHOTO).blockingGet()
            // filter out deltas that correspond to a Member that has not been synced yet
            val photoDeltasThatCanBeSynced = unsyncedPhotoDeltas
                    .filter { !unsyncedMemberIds.contains(it.modelId) }

            photoDeltasThatCanBeSynced.groupBy { it.modelId }.values.map { groupedDeltas ->
                Completable.fromAction {
                    photoRepository.sync(groupedDeltas).blockingAwait()
                    deltaRepository.markAsSynced(groupedDeltas).blockingAwait()
                }.onErrorComplete {
                    onError(it.cause ?: it)
                }.blockingAwait()
            }
        }
    }
}

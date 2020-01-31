package org.watsi.enrollment.domain.usecases

import io.reactivex.Flowable
import org.watsi.enrollment.domain.repositories.DeltaRepository

class SyncStatusUseCase(private val deltaRepository: DeltaRepository) {
    fun execute(): Flowable<DeltaRepository.SyncStatus> {
        return deltaRepository.syncStatus()
    }
}

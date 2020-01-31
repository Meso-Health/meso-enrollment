package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.repositories.DeltaRepository
import org.watsi.enrollment.domain.repositories.MemberRepository

class SyncMemberUseCase(
        private val memberRepository: MemberRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(onError: (throwable: Throwable) -> Boolean): Completable {
        return Completable.fromAction {
            val unsyncedMemberDeltas = deltaRepository.unsynced(Delta.ModelName.MEMBER).blockingGet()
            unsyncedMemberDeltas.groupBy { it.modelId }.values.map { groupedDeltas ->
                Completable.fromAction {
                    memberRepository.sync(groupedDeltas).blockingAwait()
                    deltaRepository.markAsSynced(groupedDeltas).blockingAwait()
                }.onErrorComplete {
                    onError(it.cause ?: it)
                }.blockingAwait()
            }
        }.subscribeOn(Schedulers.io())
    }
}

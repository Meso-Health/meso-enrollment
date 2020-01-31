package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.repositories.DeltaRepository
import org.watsi.enrollment.domain.repositories.MembershipPaymentRepository

class SyncMembershipPaymentUseCase(
    private val membershipPaymentRepository: MembershipPaymentRepository,
    private val deltaRepository: DeltaRepository
) {
    fun execute(onError: (throwable: Throwable) -> Boolean): Completable {
        return Completable.fromAction {
            val unsyncedMembershipDeltas = deltaRepository.unsynced(Delta.ModelName.MEMBERSHIP_PAYMENT).blockingGet()
            unsyncedMembershipDeltas.groupBy { it.modelId }.values.forEach { deltas ->
                Completable.fromAction {
                    membershipPaymentRepository.sync(deltas).blockingAwait()
                    deltaRepository.markAsSynced(deltas).blockingAwait()
                }.onErrorComplete {
                    onError(it.cause ?: it)
                }.blockingAwait()
            }
        }.subscribeOn(Schedulers.io())
    }
}

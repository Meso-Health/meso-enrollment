package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.repositories.DeltaRepository
import org.watsi.enrollment.domain.repositories.MemberEnrollmentRecordRepository

class SyncMemberEnrollmentRecordUseCase(
    private val memberEnrollmentRecordRepository: MemberEnrollmentRecordRepository,
    private val deltaRepository: DeltaRepository
) {
    fun execute(onError: (throwable: Throwable) -> Boolean): Completable {
        return Completable.fromAction {
            val unsyncedMemberEnrollmentRecordDeltas = deltaRepository.unsynced(Delta.ModelName.MEMBER_ENROLLMENT_RECORD).blockingGet()
            unsyncedMemberEnrollmentRecordDeltas.groupBy { it.modelId }.values.forEach { deltas ->
                Completable.fromAction {
                    memberEnrollmentRecordRepository.sync(deltas).blockingAwait()
                    deltaRepository.markAsSynced(deltas).blockingAwait()
                }.onErrorComplete {
                    onError(it.cause ?: it)
                }.blockingAwait()
            }
        }.subscribeOn(Schedulers.io())
    }
}

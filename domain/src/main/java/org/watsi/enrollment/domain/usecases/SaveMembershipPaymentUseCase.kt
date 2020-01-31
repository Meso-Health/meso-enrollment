package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.MembershipPayment
import org.watsi.enrollment.domain.repositories.MembershipPaymentRepository

class SaveMembershipPaymentUseCase(
    private val membershipPaymentRepository: MembershipPaymentRepository,
    private val updateMemberUseCase: UpdateMemberUseCase
) {

    fun execute(membershipPayment: MembershipPayment, members: List<Member> = emptyList()): Completable {
        val delta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.MEMBERSHIP_PAYMENT,
            modelId = membershipPayment.id
        )

        val updateMemberCompletables = members.map { updateMemberUseCase.execute(it) }

        return Completable.concatArray(
            membershipPaymentRepository.save(membershipPayment, delta),
            *updateMemberCompletables.toTypedArray()
        )
    }
}

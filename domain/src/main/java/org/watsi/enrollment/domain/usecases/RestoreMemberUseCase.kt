package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.repositories.MemberRepository

class RestoreMemberUseCase(private val memberRepository: MemberRepository) {

    fun execute(member: Member): Completable {
        val restoredMember = member.copy(archivedReason = null, archivedAt = null)
        val deltas = restoredMember.diff(member)

        return memberRepository.update(restoredMember, deltas)
    }
}

package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import org.threeten.bp.Clock
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.repositories.MemberRepository

class ArchiveMemberUseCase(private val memberRepository: MemberRepository) {

    fun execute(member: Member, archivedReason: String, clock: Clock): Completable {
        val archivedMember = member.copy(archivedAt = clock.instant(), archivedReason = archivedReason)
        val deltas = archivedMember.diff(member)

        return memberRepository.update(archivedMember, deltas)
    }
}

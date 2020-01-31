package org.watsi.enrollment.domain.usecases

import io.reactivex.Maybe
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.repositories.MemberRepository

class LoadMembersWithCardUseCase(private val memberRepository: MemberRepository) {

    fun execute(cardId: String): Maybe<Member> {
        return memberRepository.withCardId(cardId)
    }
}

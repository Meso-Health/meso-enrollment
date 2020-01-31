package org.watsi.enrollment.domain.usecases

import io.reactivex.Maybe
import org.watsi.enrollment.domain.repositories.MemberRepository
import java.util.UUID

class FindHouseholdIdByMembershipNumberUseCase(
        private val memberRepository: MemberRepository
) {
    fun execute(membershipNumber: String): Maybe<UUID> {
        return memberRepository.withMembershipNumber(membershipNumber).map { it.householdId }
    }
}

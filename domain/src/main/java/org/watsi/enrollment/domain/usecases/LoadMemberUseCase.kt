package org.watsi.enrollment.domain.usecases

import io.reactivex.Flowable
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import org.watsi.enrollment.domain.repositories.MemberRepository
import java.util.UUID

class LoadMemberUseCase(private val memberRepository: MemberRepository) {

    fun execute(parameter: UUID): Flowable<MemberWithThumbnail> {
        return memberRepository.getFlowable(parameter)
    }
}

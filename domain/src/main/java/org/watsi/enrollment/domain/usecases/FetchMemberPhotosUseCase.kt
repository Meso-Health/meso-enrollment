package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import org.watsi.enrollment.domain.repositories.MemberRepository

class FetchMemberPhotosUseCase(private val memberRepository: MemberRepository) {
    fun execute(): Completable {
        return memberRepository.downloadPhotos()
    }
}

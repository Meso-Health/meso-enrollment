package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.repositories.MemberRepository

class CreateMemberUseCase(private val memberRepository: MemberRepository) {

    fun execute(member: Member): Completable {
        val deltas = mutableListOf<Delta>()
        deltas.add(Delta(action = Delta.Action.ADD,
                         modelName = Delta.ModelName.MEMBER,
                         modelId = member.id))

        if (member.photoId != null) {
            // use member ID in photo delta because it makes it allows a more simple pattern
            // for querying the delta and creating the sync request
            deltas.add(Delta(action = Delta.Action.ADD,
                             modelName = Delta.ModelName.PHOTO,
                             modelId = member.id))
        }

        return memberRepository.create(member, deltas)
    }
}

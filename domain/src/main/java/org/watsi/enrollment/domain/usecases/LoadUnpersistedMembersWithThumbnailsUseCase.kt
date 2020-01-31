package org.watsi.enrollment.domain.usecases

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.relations.MemberWithThumbnail


class LoadUnpersistedMembersWithThumbnailsUseCase(private val loadPhotoUseCase: LoadPhotoUseCase) {

    fun execute(members: List<Member>): Single<List<MemberWithThumbnail>> {
        return Single.fromCallable {
            members.map { member ->
                val photo = member.thumbnailPhotoId?.let { photoId ->
                    loadPhotoUseCase.execute(photoId).blockingGet()
                }
                MemberWithThumbnail(member, photo)
            }
        }.subscribeOn(Schedulers.io())
    }
}

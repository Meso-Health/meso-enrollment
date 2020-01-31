package org.watsi.enrollment.domain.factories

import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.relations.MemberWithThumbnail

object MemberWithThumbnailFactory {
    fun build(member: Member): MemberWithThumbnail {
        val photoId = member.thumbnailPhotoId
        return if (photoId != null) {
            MemberWithThumbnail(member, PhotoFactory.build(photoId))
        } else {
            MemberWithThumbnail(member, null)
        }
    }
}

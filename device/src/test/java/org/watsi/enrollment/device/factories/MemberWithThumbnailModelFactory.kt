package org.watsi.enrollment.device.factories

import org.watsi.enrollment.device.db.models.MemberModel
import org.watsi.enrollment.device.db.models.MemberWithThumbnailModel

object MemberWithThumbnailModelFactory {
    fun build(memberModel: MemberModel): MemberWithThumbnailModel {
        val photoId = memberModel.thumbnailPhotoId
        return if (photoId != null) {
            val photoModel = PhotoModelFactory.build(photoId, memberModel.createdAt, memberModel.updatedAt)
            MemberWithThumbnailModel(memberModel, listOf(photoModel))
        } else {
            MemberWithThumbnailModel(memberModel, null)
        }
    }
}

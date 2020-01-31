package org.watsi.enrollment.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.enrollment.domain.relations.MemberWithThumbnail

data class MemberWithThumbnailModel(
        @Embedded var memberModel: MemberModel? = null,
        @Relation(parentColumn = "thumbnailPhotoId", entityColumn = "id", entity = PhotoModel::class)
        var photoModels: List<PhotoModel>? = null) {

    fun toMemberWithThumbnail(): MemberWithThumbnail {
        memberModel?.let {
            return MemberWithThumbnail(it.toMember(), photoModels?.firstOrNull()?.toPhoto())
        }
        throw IllegalStateException("MemberModel cannot be null")
    }
}

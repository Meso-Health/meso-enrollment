package org.watsi.enrollment.device.db.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.Instant
import org.watsi.enrollment.device.factories.MemberModelFactory
import org.watsi.enrollment.device.factories.PhotoModelFactory
import java.util.UUID

class MemberWithThumbnailModelTest {

    val now = Instant.now()
    val memberModel = MemberModelFactory.build(
        householdId = UUID.randomUUID(),
        enrolledAt = now,
        cardId = "RWI123123",
        photoId = null,
        thumbnailPhotoId = null
    )

    @Test(expected = IllegalStateException::class)
    fun toMemberWithThumbnail_nullMemberModel() {
        val memberWithThumbnailModel = MemberWithThumbnailModel(null, emptyList())
        memberWithThumbnailModel.toMemberWithThumbnail()
    }

    @Test
    fun toMemberWithThumbnail_nullPhotoModels() {
        val memberWithThumbnailModel = MemberWithThumbnailModel(memberModel, null)

        val memberWithPhotoEntity = memberWithThumbnailModel.toMemberWithThumbnail()

        assertEquals(memberModel.toMember(), memberWithPhotoEntity.member)
        assertNull(memberWithPhotoEntity.photo)
    }

    @Test
    fun toMemberWithThumbnail_withMemberAndEmptyPhotoModels() {
        val memberWithThumbnailModel = MemberWithThumbnailModel(memberModel, emptyList())

        val memberWithThumbnail = memberWithThumbnailModel.toMemberWithThumbnail()

        assertEquals(memberModel.toMember(), memberWithThumbnail.member)
        assertNull(memberWithThumbnail.photo)
    }

    @Test
    fun toMemberWithThumbnail_withMemberAndPhotoModels() {
        val photoModel = PhotoModelFactory.build(id = UUID.randomUUID())
        val memberWithThumbnailModel = MemberWithThumbnailModel(memberModel, listOf(photoModel))

        val memberWithThumbnail = memberWithThumbnailModel.toMemberWithThumbnail()

        assertEquals(memberModel.toMember(), memberWithThumbnail.member)
        assertEquals(photoModel.toPhoto(), memberWithThumbnail.photo)
    }
}

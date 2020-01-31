package org.watsi.enrollment.device.db.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.enrollment.device.factories.PhotoModelFactory
import org.watsi.enrollment.domain.entities.Photo
import org.watsi.enrollment.domain.factories.PhotoFactory
import java.util.UUID

class PhotoModelTest {

    val now = Instant.now()
    val clock = Clock.fixed(now, ZoneId.systemDefault())
    val before = Instant.ofEpochMilli(clock.instant().toEpochMilli() - 10000)

    @Test
    fun equals_differentId_returnsFalse() {
        val bytes = ByteArray(1, { 0xa })
        val p1 = PhotoModel(UUID.randomUUID(), before, before, bytes)
        val p2 = PhotoModel(UUID.randomUUID(), before, before, bytes)

        assertFalse(p1 == p2)
    }

    @Test
    fun equals_differentArray_returnsFalse() {
        val id = UUID.randomUUID()
        val p1 = PhotoModel(id, before, before, ByteArray(1, { 0xa }))
        val p2 = PhotoModel(id, before, before, ByteArray(1, { 0xb }))

        assertFalse(p1 == p2)
    }

    @Test
    fun equals_differentCreatedAt_returnsFalse() {
        val id = UUID.randomUUID()
        val p1 = PhotoModel(id, before, before, ByteArray(1, { 0xa }))
        val p2 = PhotoModel(id, Instant.now(), before, ByteArray(1, { 0xa }))

        assertFalse(p1 == p2)
    }

    @Test
    fun equals_differentUpdatedAt_returnsFalse() {
        val id = UUID.randomUUID()
        val p1 = PhotoModel(id, before, before, ByteArray(1, { 0xa }))
        val p2 = PhotoModel(id, before, Instant.now(), ByteArray(1, { 0xa }))

        assertFalse(p1 == p2)
    }

    @Test
    fun equals_allPropertiesEqual_returnsTrue() {
        val id = UUID.randomUUID()
        val p1 = PhotoModel(id, before, before, ByteArray(1, { 0xa }))
        val p2 = PhotoModel(id, before, before, ByteArray(1, { 0xa }))

        assertTrue(p1 == p2)
    }

    @Test
    fun toPhoto() {
        val photoModel = PhotoModelFactory.build()
        assertEquals(Photo(photoModel.id, photoModel.bytes), photoModel.toPhoto())
    }

    @Test
    fun fromPhoto() {
        val photo = PhotoFactory.build()
        val photoModel = PhotoModel(id = photo.id,
                                    bytes = photo.bytes,
                                    createdAt = now,
                                    updatedAt = now)

        assertEquals(photoModel, PhotoModel.fromPhoto(photo, clock))
    }
}

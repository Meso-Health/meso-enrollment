package org.watsi.enrollment.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.enrollment.device.db.daos.PhotoDao
import org.watsi.enrollment.device.db.models.PhotoModel
import java.util.UUID

object PhotoModelFactory {

    fun build(
            id: UUID = UUID.randomUUID(),
            createdAt: Instant? = null,
            updatedAt: Instant? = null,
            bytes: ByteArray = ByteArray(1, { 0xa }),
            clock: Clock = Clock.systemUTC()
    ): PhotoModel {
        val currentTime = Instant.now(clock)
        return PhotoModel(
                id = id,
                createdAt = createdAt ?: currentTime,
                updatedAt = updatedAt ?: currentTime,
                bytes = bytes)
    }

    fun create(
            photoDao: PhotoDao,
            id: UUID = UUID.randomUUID(),
            createdAt: Instant? = null,
            updatedAt: Instant? = null,
            bytes: ByteArray = ByteArray(1, { 0xa }),
            clock: Clock = Clock.systemUTC()
    ): PhotoModel {
        val photoModel = build(id, createdAt, updatedAt, bytes, clock)
        photoDao.insert(photoModel)
        return photoModel
    }
}

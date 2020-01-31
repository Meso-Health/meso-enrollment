package org.watsi.enrollment.domain.factories

import org.watsi.enrollment.domain.entities.Photo
import java.util.UUID

object PhotoFactory {

    fun build(
            id: UUID = UUID.randomUUID(),
            bytes: ByteArray = ByteArray(1, { 0xa })
    ): Photo {
        return Photo(id, bytes)
    }
}

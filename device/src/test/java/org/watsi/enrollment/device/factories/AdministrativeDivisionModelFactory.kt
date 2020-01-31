package org.watsi.enrollment.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.enrollment.device.db.daos.AdministrativeDivisionDao
import org.watsi.enrollment.device.db.models.AdministrativeDivisionModel

object AdministrativeDivisionModelFactory {
    fun build(
        id: Int = 0,
        name: String = "Village A",
        level: String = "subvillage",
        code: String? = null,
        parentId: Int? = null,
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        clock: Clock = Clock.systemUTC()
    ): AdministrativeDivisionModel {
        val currentTime = Instant.now(clock)
        return AdministrativeDivisionModel(
            id = id,
            name = name,
            level = level,
            code = code,
            parentId = parentId,
            createdAt = createdAt ?: currentTime,
            updatedAt = updatedAt ?: currentTime
        )
    }

    fun create(
        administrativeDivisionDao: AdministrativeDivisionDao,
        id: Int = 0,
        name: String = "Village A",
        level: String = "subvillage",
        code: String? = null,
        parentId: Int? = null,
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        clock: Clock = Clock.systemUTC()
    ): AdministrativeDivisionModel {
        val administrativeDivisionModel = build(
            id = id,
            name = name,
            level = level,
            code = code,
            parentId = parentId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            clock = clock
        )
        administrativeDivisionDao.upsert(listOf(administrativeDivisionModel))
        return administrativeDivisionModel
    }
}

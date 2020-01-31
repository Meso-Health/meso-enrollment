package org.watsi.enrollment.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.AdministrativeDivision

@Entity(tableName = "administrativeDivisions")
data class AdministrativeDivisionModel(
    @PrimaryKey val id: Int = 0,
    val name: String,
    val level: String,
    val code: String?,
    val parentId: Int?,
    val createdAt: Instant,
    val updatedAt: Instant
) {

    fun toAdministrativeDivision(): AdministrativeDivision {
        return AdministrativeDivision(
            id = id,
            name = name,
            level = level,
            code = code,
            parentId = parentId
        )
    }

    companion object {
        fun fromAdministrativeDivision(
            administrativeDivision: AdministrativeDivision,
            clock: Clock = Clock.systemDefaultZone()
        ): AdministrativeDivisionModel {
            val now = clock.instant()
            return AdministrativeDivisionModel(
                id = administrativeDivision.id,
                name = administrativeDivision.name,
                level = administrativeDivision.level,
                code = administrativeDivision.code,
                parentId = administrativeDivision.parentId,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

package org.watsi.enrollment.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.enrollment.domain.entities.EnrollmentPeriod

@Entity(tableName = "enrollmentPeriods")
data class EnrollmentPeriodModel(
    @PrimaryKey val id: Int = 0,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val createdAt: Instant,
    val updatedAt: Instant
) {

    fun toEnrollmentPeriod(): EnrollmentPeriod {
        return EnrollmentPeriod(
            id = id,
            startDate = startDate,
            endDate = endDate
        )
    }

    companion object {
        fun fromEnrollmentPeriod(
            enrollmentPeriod: EnrollmentPeriod,
            clock: Clock
        ): EnrollmentPeriodModel {
            val now = clock.instant()
            return EnrollmentPeriodModel(
                id = enrollmentPeriod.id,
                startDate = enrollmentPeriod.startDate,
                endDate = enrollmentPeriod.endDate,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

package org.watsi.enrollment.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.HouseholdEnrollmentRecord
import java.util.UUID

@Entity(tableName = "householdEnrollmentRecords",
        indices = [
            Index("householdId"),
            Index("enrollmentPeriodId")
        ],
        foreignKeys = [
            ForeignKey(
                entity = HouseholdModel::class,
                parentColumns = ["id"],
                childColumns = ["householdId"]
            ),
            ForeignKey(
                entity = EnrollmentPeriodModel::class,
                parentColumns = ["id"],
                childColumns = ["enrollmentPeriodId"]
            ),
            ForeignKey(
                entity = AdministrativeDivisionModel::class,
                parentColumns = ["id"],
                childColumns = ["administrativeDivisionId"]
            )
        ]
)data class HouseholdEnrollmentRecordModel(
    @PrimaryKey val id: UUID,
    val householdId: UUID,
    val userId: Int,
    val enrolledAt: Instant,
    val paying: Boolean,
    val renewal: Boolean,
    val administrativeDivisionId: Int,
    val enrollmentPeriodId: Int,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    fun toHouseholdEnrollmentRecord(): HouseholdEnrollmentRecord {
        return HouseholdEnrollmentRecord(
            id = id,
            householdId = householdId,
            userId = userId,
            enrolledAt = enrolledAt,
            paying = paying,
            renewal = renewal,
            administrativeDivisionId = administrativeDivisionId,
            enrollmentPeriodId = enrollmentPeriodId
        )
    }

    companion object {
        fun fromHouseholdEnrollmentRecord(
            householdEnrollmentRecord: HouseholdEnrollmentRecord, clock: Clock
        ): HouseholdEnrollmentRecordModel {
            val now = clock.instant()
            return HouseholdEnrollmentRecordModel(
                id = householdEnrollmentRecord.id,
                householdId = householdEnrollmentRecord.householdId,
                userId = householdEnrollmentRecord.userId,
                enrolledAt = householdEnrollmentRecord.enrolledAt,
                paying = householdEnrollmentRecord.paying,
                renewal = householdEnrollmentRecord.renewal,
                administrativeDivisionId = householdEnrollmentRecord.administrativeDivisionId,
                enrollmentPeriodId = householdEnrollmentRecord.enrollmentPeriodId,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

package org.watsi.enrollment.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.MemberEnrollmentRecord
import java.util.UUID

@Entity(tableName = "memberEnrollmentRecords")
data class MemberEnrollmentRecordModel(
    @PrimaryKey val id: UUID,
    val memberId: UUID,
    val userId: Int,
    val enrolledAt: Instant,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val enrollmentPeriodId: Int
) {
    fun toMemberEnrollmentRecord(): MemberEnrollmentRecord {
        return MemberEnrollmentRecord(
            id = id,
            memberId = memberId,
            userId = userId,
            enrolledAt = enrolledAt,
            note = note,
            enrollmentPeriodId = enrollmentPeriodId
        )
    }

    companion object {
        fun fromMemberEnrollmentRecord(
            memberEnrollmentRecord: MemberEnrollmentRecord, clock: Clock
        ): MemberEnrollmentRecordModel {
            val now = clock.instant()
            return MemberEnrollmentRecordModel(
                id = memberEnrollmentRecord.id,
                memberId = memberEnrollmentRecord.memberId,
                userId = memberEnrollmentRecord.userId,
                enrolledAt = memberEnrollmentRecord.enrolledAt,
                note = memberEnrollmentRecord.note,
                enrollmentPeriodId = memberEnrollmentRecord.enrollmentPeriodId,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

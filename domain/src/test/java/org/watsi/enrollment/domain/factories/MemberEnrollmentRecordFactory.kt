package org.watsi.enrollment.domain.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.MemberEnrollmentRecord
import java.util.UUID

object MemberEnrollmentRecordFactory {

    fun build(
        id: UUID = UUID.randomUUID(),
        memberId: UUID = UUID.randomUUID(),
        userId: Int = 1,
        enrolledAt: Instant? = null,
        note: String? = null,
        enrollmentRecordId: Int = 1,
        clock: Clock = Clock.systemUTC()
    ): MemberEnrollmentRecord {
        val currentTime = Instant.now(clock)
        return MemberEnrollmentRecord(
            id = id,
            memberId = memberId,
            userId = userId,
            enrolledAt = enrolledAt ?: currentTime,
            note = note,
            enrollmentPeriodId = enrollmentRecordId
        )
    }
}

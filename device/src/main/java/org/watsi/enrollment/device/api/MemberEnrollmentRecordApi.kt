package org.watsi.enrollment.device.api

import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.MemberEnrollmentRecord
import java.util.UUID

data class MemberEnrollmentRecordApi(
    val id: UUID,
    val memberId: UUID,
    val userId: Int,
    val enrolledAt: Instant,
    val note: String?,
    val membershipNumber: String? = null,
    val enrollmentPeriodId: Int
) {
    constructor(memberEnrollmentRecord: MemberEnrollmentRecord): this(
        id = memberEnrollmentRecord.id,
        memberId = memberEnrollmentRecord.memberId,
        userId = memberEnrollmentRecord.userId,
        enrolledAt = memberEnrollmentRecord.enrolledAt,
        note = memberEnrollmentRecord.note,
        enrollmentPeriodId = memberEnrollmentRecord.enrollmentPeriodId
    )

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
}

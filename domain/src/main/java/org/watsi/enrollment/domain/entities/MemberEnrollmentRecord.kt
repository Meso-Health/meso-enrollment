package org.watsi.enrollment.domain.entities

import org.threeten.bp.Instant
import java.util.UUID

data class MemberEnrollmentRecord(
    val id: UUID,
    val memberId: UUID,
    val userId: Int,
    val enrolledAt: Instant,
    val note: String?,
    val enrollmentPeriodId: Int
)

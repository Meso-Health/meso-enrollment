package org.watsi.enrollment.domain.entities

import org.threeten.bp.Instant
import java.io.Serializable
import java.util.UUID

data class HouseholdEnrollmentRecord(
    val id: UUID,
    val householdId: UUID,
    val userId: Int,
    val enrolledAt: Instant,
    val paying: Boolean,
    val renewal: Boolean,
    val administrativeDivisionId: Int,
    val enrollmentPeriodId: Int
): Serializable

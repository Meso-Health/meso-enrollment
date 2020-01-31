package org.watsi.enrollment.device.api

import org.threeten.bp.Instant
import java.util.UUID

/**
 * Data class that defines the structure of a GET /households API request
 */
data class FetchHouseholdApi(
    val id: UUID,
    val administrativeDivisionId: Int,
    val enrolledAt: Instant,
    val members: List<MemberApi>,
    val activeMembershipPayments: List<MembershipPaymentApi>,
    val memberEnrollmentRecords: List<MemberEnrollmentRecordApi>,
    val activeHouseholdEnrollmentRecord: HouseholdEnrollmentRecordApi?,
    val address: String?
)

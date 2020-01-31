package org.watsi.enrollment.device.api

import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.HouseholdEnrollmentRecord
import java.util.UUID

data class HouseholdEnrollmentRecordApi(
    val id: UUID,
    val householdId: UUID,
    val userId: Int,
    val enrolledAt: Instant,
    val paying: Boolean,
    val renewal: Boolean,
    val administrativeDivisionId: Int,
    val enrollmentPeriodId: Int
) {
    constructor(householdEnrollmentRecord: HouseholdEnrollmentRecord): this(
        id = householdEnrollmentRecord.id,
        householdId = householdEnrollmentRecord.householdId,
        userId = householdEnrollmentRecord.userId,
        enrolledAt = householdEnrollmentRecord.enrolledAt,
        paying = householdEnrollmentRecord.paying,
        renewal = householdEnrollmentRecord.renewal,
        administrativeDivisionId = householdEnrollmentRecord.administrativeDivisionId,
        enrollmentPeriodId = householdEnrollmentRecord.enrollmentPeriodId
    )

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
}

package org.watsi.enrollment.domain.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.HouseholdEnrollmentRecord
import java.util.UUID

object HouseholdEnrollmentRecordFactory {

    fun build(
        id: UUID = UUID.randomUUID(),
        householdId: UUID = UUID.randomUUID(),
        userId: Int = 1,
        enrolledAt: Instant? = null,
        paying: Boolean = true,
        renewal: Boolean = false,
        administrativeDivisionId: Int = 1,
        enrollmentPeriodId: Int = 1,
        clock: Clock = Clock.systemUTC()
    ): HouseholdEnrollmentRecord {
        val currentTime = Instant.now(clock)
        return HouseholdEnrollmentRecord(
            id = id,
            householdId = householdId,
            userId = userId,
            enrolledAt = enrolledAt ?: currentTime,
            paying = paying,
            renewal = renewal,
            administrativeDivisionId = administrativeDivisionId,
            enrollmentPeriodId = enrollmentPeriodId
        )
    }
}

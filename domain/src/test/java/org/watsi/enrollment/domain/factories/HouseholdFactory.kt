package org.watsi.enrollment.domain.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.Household
import java.util.UUID

object HouseholdFactory {

    fun build(
        id: UUID = UUID.randomUUID(),
        enrolledAt: Instant? = null,
        administrativeDivisionId: Int = 1,
        address: String? = null,
        clock: Clock = Clock.systemUTC()
    ): Household {
        val currentTime = Instant.now(clock)
        return Household(
            id = id,
            enrolledAt = enrolledAt ?: currentTime,
            administrativeDivisionId = administrativeDivisionId,
            address = address
        )
    }
}

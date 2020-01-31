package org.watsi.enrollment.device.api

import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.Household
import java.util.UUID

/**
 * Data class that defines the structure of a sync Household API request
 */
data class SyncHouseholdApi(
    val id: UUID,
    val enrolledAt: Instant,
    val administrativeDivisionId: Int,
    val address: String?
) {
    constructor(household: Household): this(
        id = household.id,
        enrolledAt = household.enrolledAt,
        administrativeDivisionId = household.administrativeDivisionId,
        address = household.address
    )
}

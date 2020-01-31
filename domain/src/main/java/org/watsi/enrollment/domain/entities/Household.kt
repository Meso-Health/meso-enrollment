package org.watsi.enrollment.domain.entities

import org.threeten.bp.Instant
import java.io.Serializable
import java.util.UUID

/**
 *  Normalized representation of a household.
 */
data class Household(
    val id: UUID,
    val enrolledAt: Instant,
    val administrativeDivisionId: Int,
    val address: String?
) : Serializable

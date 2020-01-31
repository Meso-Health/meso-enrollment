package org.watsi.enrollment.domain.relations

import org.watsi.enrollment.domain.entities.AdministrativeDivision
import org.watsi.enrollment.domain.entities.Household
import java.io.Serializable

/**
 *  Representation of a Household with members that have photos in them
 */
data class HouseholdWithMembers(
    val household: Household,
    val members: List<MemberWithThumbnail>,
    val administrativeDivision: AdministrativeDivision?
) : Serializable

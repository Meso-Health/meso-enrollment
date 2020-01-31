package org.watsi.enrollment.domain.factories

import org.watsi.enrollment.domain.entities.AdministrativeDivision
import org.watsi.enrollment.domain.entities.Household
import org.watsi.enrollment.domain.relations.HouseholdWithMembers
import org.watsi.enrollment.domain.relations.MemberWithThumbnail

object HouseholdWithMembersFactory {
    fun build(
        household: Household = HouseholdFactory.build(),
        members: List<MemberWithThumbnail> = listOf(),
        administrativeDivision: AdministrativeDivision? = null
    ): HouseholdWithMembers {
        return HouseholdWithMembers(household, members, administrativeDivision)
    }
}

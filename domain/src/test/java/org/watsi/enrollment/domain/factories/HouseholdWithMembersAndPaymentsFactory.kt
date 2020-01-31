package org.watsi.enrollment.domain.factories

import org.watsi.enrollment.domain.entities.AdministrativeDivision
import org.watsi.enrollment.domain.entities.Household
import org.watsi.enrollment.domain.entities.HouseholdEnrollmentRecord
import org.watsi.enrollment.domain.entities.MembershipPayment
import org.watsi.enrollment.domain.relations.HouseholdWithMembersAndPayments
import org.watsi.enrollment.domain.relations.MemberWithThumbnail

object HouseholdWithMembersAndPaymentsFactory {
    fun build(
        household: Household = HouseholdFactory.build(),
        householdEnrollmentRecords: List<HouseholdEnrollmentRecord> = emptyList(),
        members: List<MemberWithThumbnail> = listOf(),
        payments: List<MembershipPayment> = emptyList(),
        administrativeDivision: AdministrativeDivision? = null
    ): HouseholdWithMembersAndPayments {
        return HouseholdWithMembersAndPayments(
            household,
            householdEnrollmentRecords,
            members,
            payments,
            administrativeDivision
        )
    }
}

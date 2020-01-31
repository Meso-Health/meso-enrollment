package org.watsi.enrollment.flowstates

import org.watsi.enrollment.domain.entities.AdministrativeDivision
import org.watsi.enrollment.domain.entities.EnrollmentPeriod
import org.watsi.enrollment.domain.entities.Household
import org.watsi.enrollment.domain.entities.HouseholdEnrollmentRecord
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.MembershipPayment
import org.watsi.enrollment.domain.relations.HouseholdWithMembersAndPayments
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import java.io.Serializable

data class HouseholdFlowState(
    var household: Household,
    val householdEnrollmentRecords: List<HouseholdEnrollmentRecord>,
    var members: MutableList<Member>,
    var payments: MutableList<MembershipPayment>,
    val manualMembershipNumber: String? = null, // only used during manual renewals
    val administrativeDivision: AdministrativeDivision? = null
) : Serializable {

    fun toHouseholdWithMembersAndPayment(): HouseholdWithMembersAndPayments {
        return HouseholdWithMembersAndPayments(
            household = household,
            householdEnrollmentRecords = householdEnrollmentRecords,
            members = members.toList().map {
                MemberWithThumbnail(it, null)
            },
            payments = payments.toList(),
            administrativeDivision = administrativeDivision
        )
    }

    fun activeHouseholdEnrollmentRecord(enrollmentPeriod: EnrollmentPeriod): HouseholdEnrollmentRecord? {
        return toHouseholdWithMembersAndPayment().activeHouseholdEnrollmentRecord(enrollmentPeriod)
    }

    fun wasPayingMostRecentPeriod(): Boolean? {
        return toHouseholdWithMembersAndPayment().wasPayingMostRecentPeriod()
    }

    fun cardIds(): ArrayList<String> {
        return ArrayList(members.mapNotNull { it.cardId })
    }
}

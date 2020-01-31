package org.watsi.enrollment.domain.relations

import org.watsi.enrollment.domain.entities.AdministrativeDivision
import org.watsi.enrollment.domain.entities.EnrollmentPeriod
import org.watsi.enrollment.domain.entities.Household
import org.watsi.enrollment.domain.entities.HouseholdEnrollmentRecord
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.MembershipPayment
import org.watsi.enrollment.domain.entities.PaymentFees
import java.io.Serializable

/**
 *  Representation of a Household with members that have photos in them and list of payments.
 */
data class HouseholdWithMembersAndPayments(
    val household: Household,
    val householdEnrollmentRecords: List<HouseholdEnrollmentRecord>,
    val members: List<MemberWithThumbnail>,
    val payments: List<MembershipPayment> = emptyList(),
    val administrativeDivision: AdministrativeDivision?
) : Serializable {

    fun isPaying(enrollmentPeriod: EnrollmentPeriod): Boolean? {
        return activeHouseholdEnrollmentRecord(enrollmentPeriod)?.paying
    }

    fun wasPayingMostRecentPeriod(): Boolean? {
        val mostRecentEnrollmentRecord = householdEnrollmentRecords.maxBy { it.enrolledAt }
        return mostRecentEnrollmentRecord?.paying
    }

    fun needsPayment(enrollmentPeriod: EnrollmentPeriod): Boolean {
        return isPaying(enrollmentPeriod) == true && this.members.any { it.member.unpaid() }
    }

    fun needsRenewal(enrollmentPeriod: EnrollmentPeriod): Boolean {
        return activeHouseholdEnrollmentRecord(enrollmentPeriod) == null
    }

    fun activeHouseholdEnrollmentRecord(enrollmentPeriod: EnrollmentPeriod):
            HouseholdEnrollmentRecord? {
        return householdEnrollmentRecords.find { it.enrollmentPeriodId == enrollmentPeriod.id }
    }

    fun paymentsThisPeriod(enrollmentPeriod: EnrollmentPeriod): List<MembershipPayment> {
        val activeEnrollmentRecord = activeHouseholdEnrollmentRecord(enrollmentPeriod)
        return payments.filter { it.householdEnrollmentRecordId == activeEnrollmentRecord?.id }
    }

    fun memberRequiresFee(
        member: Member,
        enrollmentPeriod: EnrollmentPeriod
    ): Boolean {
        if (isPaying(enrollmentPeriod) != true) return false

        // Create a version of the household where this member is included
        val updatedMembersList = members.toMutableList()
        updatedMembersList.remove(members.find { it.member.id == member.id })
        updatedMembersList.add(MemberWithThumbnail(member, null))
        val updatedHousehold = HouseholdWithMembersAndPayments(
            household = this.household,
            householdEnrollmentRecords = this.householdEnrollmentRecords,
            payments = this.payments,
            members = updatedMembersList,
            administrativeDivision = this.administrativeDivision
        )

        return PaymentFees.calculate(
            householdWithMembersAndPayments = updatedHousehold,
            isPaying = isPaying(enrollmentPeriod) == true,
            isReplacingCard = false,
            isNewEnrollment = false,
            penalty = 0,
            other = 0,
            enrollmentPeriod = enrollmentPeriod
        ).beneficiariesFee > 0
    }

    fun unarchivedMembers(): List<MemberWithThumbnail> {
        return members.filter { it.member.archivedAt == null }
    }

    fun unarchivedAndUnpaidMembers(): List<MemberWithThumbnail> {
        return members.filter { it.member.archivedReason == null || it.member.unpaid() }
    }
}

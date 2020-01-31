package org.watsi.enrollment.domain.relations

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.PaymentFees
import org.watsi.enrollment.domain.factories.EnrollmentPeriodFactory
import org.watsi.enrollment.domain.factories.HouseholdEnrollmentRecordFactory
import org.watsi.enrollment.domain.factories.HouseholdFactory
import org.watsi.enrollment.domain.factories.HouseholdWithMembersAndPaymentsFactory
import org.watsi.enrollment.domain.factories.MemberFactory
import org.watsi.enrollment.domain.factories.MemberWithThumbnailFactory
import org.watsi.enrollment.domain.factories.MembershipPaymentFactory

class HouseholdWithMembersAndPaymentsTest {
    val enrollmentPeriod = EnrollmentPeriodFactory.build()

    @Test
    fun needsPayment_indigent_false() {
        val indigentMember = MemberFactory.build()
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(HouseholdEnrollmentRecordFactory.build(paying = false)),
            members = listOf(MemberWithThumbnail(indigentMember, null)),
            payments = emptyList()
        )

        assertFalse(householdWithMembersAndPayments.needsPayment(enrollmentPeriod))
    }

    @Test
    fun needsPayment_payingNeedsRenewal_true() {
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(paying = true)
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(householdEnrollmentRecord),
            payments = emptyList(),
            members = listOf(MemberWithThumbnail(MemberFactory.unpaidQualifyingBeneficiary(), null))
        )

        assertTrue(householdWithMembersAndPayments.needsPayment(enrollmentPeriod))
    }

    @Test
    fun needsPayment_payingRenewed_false() {
        val payingMember = MemberFactory.build()
        val payment = MembershipPaymentFactory.build(annualContributionFee = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE)
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(HouseholdEnrollmentRecordFactory.build(paying = true)),
            members = listOf(MemberWithThumbnail(payingMember, null)),
            payments = listOf(payment)
        )

        assertFalse(householdWithMembersAndPayments.needsPayment(enrollmentPeriod))
    }

    @Test
    fun needsPayment_payingRenewedThenAddedQualifyingBeneficiary_true() {
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(paying = true)
        val headOfHousehold = MemberFactory.headOfHousehold()
        val newQualifyingBeneficiary = MemberFactory.unpaidQualifyingBeneficiary()
        val payment = MembershipPaymentFactory.build(annualContributionFee = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE)
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(householdEnrollmentRecord),
            members = listOf(
                MemberWithThumbnail(headOfHousehold, null),
                MemberWithThumbnail(newQualifyingBeneficiary, null)
            ),
            payments = listOf(payment)
        )

        assertTrue(householdWithMembersAndPayments.needsPayment(enrollmentPeriod))
    }

    @Test
    fun memberRequiresFee_indigent_false() {
        val indigentMember = MemberFactory.build()
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(HouseholdEnrollmentRecordFactory.build(paying = false)),
            members = listOf(MemberWithThumbnail(indigentMember, null)),
            payments = emptyList()
        )
        val newMember = MemberFactory.qualifyingBeneficiary()

        assertFalse(householdWithMembersAndPayments.memberRequiresFee(newMember, enrollmentPeriod))
    }

    @Test
    fun memberRequiresFee_needsRenewal_true() {
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(paying = true)
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(householdEnrollmentRecord),
            payments = emptyList()
        )
        val newMember = MemberFactory.qualifyingBeneficiary()

        assertTrue(householdWithMembersAndPayments.memberRequiresFee(newMember, enrollmentPeriod))
    }

    @Test
    fun memberRequiresFee_payingThenAddedQualifyingBeneficiary_true() {
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(paying = true)
        val headOfHousehold = MemberFactory.headOfHousehold()
        val newQualifyingBeneficiary = MemberFactory.qualifyingBeneficiary()
        val payment = MembershipPaymentFactory.build(annualContributionFee = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE)
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(householdEnrollmentRecord),
            members = listOf(
                MemberWithThumbnail(headOfHousehold, null),
                MemberWithThumbnail(newQualifyingBeneficiary, null)
            ),
            payments = listOf(payment)
        )
        val newMember = MemberFactory.qualifyingBeneficiary()

        assertTrue(householdWithMembersAndPayments.memberRequiresFee(newMember, enrollmentPeriod))
    }

    @Test
    fun memberRequiresFee_paymentSurplus_false() {
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(
            paying = true,
            enrollmentPeriodId = enrollmentPeriod.id
        )
        val headOfHousehold = MemberFactory.headOfHousehold()
        val newQualifyingBeneficiary = MemberFactory.qualifyingBeneficiary()
        val payment = MembershipPaymentFactory.build(
            annualContributionFee = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE,
            qualifyingBeneficiariesFee = PaymentFees.ANNUAL_BENEFICIARY_FEE * 3,
            householdEnrollmentRecordId = householdEnrollmentRecord.id
        )
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(householdEnrollmentRecord),
            members = listOf(
                MemberWithThumbnail(headOfHousehold, null),
                MemberWithThumbnail(newQualifyingBeneficiary, null)
            ),
            payments = listOf(payment)
        )
        val newMember = MemberFactory.qualifyingBeneficiary()

        assertFalse(householdWithMembersAndPayments.memberRequiresFee(newMember, enrollmentPeriod))
    }

    @Test
    fun needsRenewal_noEnrollmentRecord_true() {
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = emptyList())

        assertTrue(householdWithMembersAndPayments.needsRenewal(enrollmentPeriod))
    }

    @Test
    fun needsRenewal_hasEnrollmentRecord_false() {
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build()
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(householdEnrollmentRecord))

        assertFalse(householdWithMembersAndPayments.needsRenewal(enrollmentPeriod))
    }

    @Test
    fun unarchivedMembers() {
        val activeMember = MemberWithThumbnailFactory.build(MemberFactory.build())
        val archivedMember = MemberWithThumbnailFactory.build(MemberFactory.build(archivedAt = Instant.now()))

        val relation = HouseholdWithMembersAndPayments(
            household = HouseholdFactory.build(),
            members = listOf(activeMember, archivedMember),
            householdEnrollmentRecords = emptyList(),
            administrativeDivision = null
        )

        assertEquals(listOf(activeMember), relation.unarchivedMembers())
    }

    @Test
    fun wasPayingMostRecentPeriod_hasPayingEnrollmentRecord_true() {
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(paying = true)
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(householdEnrollmentRecord))

        val result = householdWithMembersAndPayments.wasPayingMostRecentPeriod()
        if (result == null) Assert.fail()
        else assertTrue(result)
    }

    @Test
    fun wasPayingMostRecentPeriod_hasIndigentEnrollmentRecord_false() {
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(paying = false)
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(householdEnrollmentRecord))

        val result = householdWithMembersAndPayments.wasPayingMostRecentPeriod()
        if (result == null) Assert.fail()
        else assertFalse(result)
    }

    @Test
    fun wasPayingMostRecentPeriod_noEnrollmentRecord_payingMembershipNumber_true() {
        val member =
            MemberWithThumbnailFactory.build(MemberFactory.build())
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(HouseholdEnrollmentRecordFactory.build(paying = true)),
            members = listOf(member)
        )

        val result = householdWithMembersAndPayments.wasPayingMostRecentPeriod()
        if (result == null) Assert.fail()
        else assertTrue(result)
    }

    @Test
    fun wasPayingMostRecentPeriod_noEnrollmentRecord_indigentMembershipNumber_false() {
        val member =
            MemberWithThumbnailFactory.build(MemberFactory.build())
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(HouseholdEnrollmentRecordFactory.build(paying = false)),
            members = listOf(member)
        )

        val result = householdWithMembersAndPayments.wasPayingMostRecentPeriod()
        if (result == null) Assert.fail()
        else assertFalse(result)
    }
}

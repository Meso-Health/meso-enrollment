package org.watsi.enrollment.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.watsi.enrollment.domain.factories.EnrollmentPeriodFactory
import org.watsi.enrollment.domain.factories.HouseholdEnrollmentRecordFactory
import org.watsi.enrollment.domain.factories.HouseholdWithMembersAndPaymentsFactory
import org.watsi.enrollment.domain.factories.MemberFactory
import org.watsi.enrollment.domain.factories.MembershipPaymentFactory
import org.watsi.enrollment.domain.relations.MemberWithThumbnail

@RunWith(JUnit4::class)
class PaymentFeesTest {
    val enrollmentPeriod = EnrollmentPeriodFactory.build()

    @Test
    fun total() {
        val paymentFees = PaymentFees(1, 2, 3, 4, 5, 6, 7)

        assertEquals(24, paymentFees.total())
    }

    @Test
    fun calculate_newEnrollmentPaying_noPenaltiesNoQualifyingBeneficiaries() {
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build()

        val result = PaymentFees.calculate(
            householdWithMembersAndPayments = householdWithMembersAndPayments,
            isPaying = true,
            isReplacingCard = false,
            isNewEnrollment = true,
            penalty = 0,
            other = 0,
            enrollmentPeriod = enrollmentPeriod
        ).total()
        val expected = PaymentFees.INITIAL_ENROLLMENT_FEE + PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE

        assertEquals(expected, result)
    }

    @Test
    fun calculate_newEnrollmentPaying_withPenaltiesNoQualifyingBeneficiaries() {
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build()
        val penalty = 100
        val other = 200

        val result = PaymentFees.calculate(
            householdWithMembersAndPayments = householdWithMembersAndPayments,
            isPaying = true,
            isReplacingCard = false,
            isNewEnrollment = true,
            penalty = penalty,
            other = other,
            enrollmentPeriod = enrollmentPeriod
        ).total()
        val expected = PaymentFees.INITIAL_ENROLLMENT_FEE + PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE + penalty + other

        assertEquals(expected, result)
    }

    private val householdWithTwoQualifyingBeneficiaries = listOf(
        MemberFactory.headOfHousehold(),
        MemberFactory.qualifyingBeneficiary(),
        MemberFactory.qualifyingBeneficiary()
    )

    @Test
    fun calculate_newEnrollmentPaying_noPenaltiesWithTwoQualifyingBeneficiaries() {
        val members = householdWithTwoQualifyingBeneficiaries.map { MemberWithThumbnail(it, null) }
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            members = members)

        val result = PaymentFees.calculate(
            householdWithMembersAndPayments = householdWithMembersAndPayments,
            isPaying = true,
            isReplacingCard = false,
            isNewEnrollment = true,
            penalty = 0,
            other = 0,
            enrollmentPeriod = enrollmentPeriod
        ).total()
        val expected = PaymentFees.INITIAL_ENROLLMENT_FEE + PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE +
                (2 * PaymentFees.ANNUAL_BENEFICIARY_FEE)

        assertEquals(expected, result)
    }

    @Test
    fun calculate_newEnrollmentIndigent_noPenaltiesWithTwoQualifyingBeneficiaries() {
        val members = householdWithTwoQualifyingBeneficiaries.map { MemberWithThumbnail(it, null) }
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            members = members)

        val result = PaymentFees.calculate(
            householdWithMembersAndPayments = householdWithMembersAndPayments,
            isPaying = false,
            isReplacingCard = false,
            isNewEnrollment = true,
            penalty = 0,
            other = 0,
            enrollmentPeriod = enrollmentPeriod
        ).total()

        assertEquals(0, result)
    }

    @Test
    fun calculate_renewalPaying_noPenaltiesWithTwoQualifyingBeneficiariesAndReplacement() {
        val members = householdWithTwoQualifyingBeneficiaries.map { MemberWithThumbnail(it, null) }
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            members = members)

        val result = PaymentFees.calculate(
            householdWithMembersAndPayments = householdWithMembersAndPayments,
            isPaying = true,
            isReplacingCard = true,
            isNewEnrollment = false,
            penalty = 0,
            other = 0,
            enrollmentPeriod = enrollmentPeriod
        ).total()
        val expected = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE + PaymentFees.CARD_FEE +
                (2 * PaymentFees.ANNUAL_BENEFICIARY_FEE)

        assertEquals(expected, result)
    }

    @Test
    fun calculate_renewalIndigent_noPenaltiesWithTwoQualifyingBeneficiaries() {
        val members = householdWithTwoQualifyingBeneficiaries.map { MemberWithThumbnail(it, null) }
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            members = members)

        val result = PaymentFees.calculate(
            householdWithMembersAndPayments = householdWithMembersAndPayments,
            isPaying = false,
            isReplacingCard = false,
            isNewEnrollment = false,
            penalty = 0,
            other = 0,
            enrollmentPeriod = enrollmentPeriod
        ).total()

        assertEquals(0, result)
    }

    @Test
    fun calculate_renewedPaying_noAdditionalQualifyingBeneficiaries() {
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(
            enrollmentPeriodId = enrollmentPeriod.id)
        val previousPayment = MembershipPaymentFactory.build(
            annualContributionFee = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE,
            qualifyingBeneficiariesFee = PaymentFees.ANNUAL_BENEFICIARY_FEE * 2,
            householdEnrollmentRecordId = householdEnrollmentRecord.id
        )
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(householdEnrollmentRecord),
            members = householdWithTwoQualifyingBeneficiaries.map { MemberWithThumbnail(it, null) },
            payments = listOf(previousPayment)
        )

        val result = PaymentFees.calculate(
            householdWithMembersAndPayments = householdWithMembersAndPayments,
            isPaying = true,
            isReplacingCard = false,
            isNewEnrollment = false,
            penalty = 0,
            other = 0,
            enrollmentPeriod = enrollmentPeriod
        ).total()

        assertEquals(0, result)
    }

    @Test
    fun calculate_renewedPayingAdded_noAdditionalQualifyingBeneficiaries() {
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(
            enrollmentPeriodId = enrollmentPeriod.id)
        val previousPayment1 = MembershipPaymentFactory.build(
            annualContributionFee = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE,
            qualifyingBeneficiariesFee = PaymentFees.ANNUAL_BENEFICIARY_FEE,
            householdEnrollmentRecordId = householdEnrollmentRecord.id
        )
        val previousPayment2 = MembershipPaymentFactory.build(
            qualifyingBeneficiariesFee = PaymentFees.ANNUAL_BENEFICIARY_FEE,
            householdEnrollmentRecordId = householdEnrollmentRecord.id
        )
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(householdEnrollmentRecord),
            members = householdWithTwoQualifyingBeneficiaries.map { MemberWithThumbnail(it, null) },
            payments = listOf(previousPayment1, previousPayment2)
        )

        val result = PaymentFees.calculate(
            householdWithMembersAndPayments = householdWithMembersAndPayments,
            isPaying = true,
            isReplacingCard = false,
            isNewEnrollment = false,
            penalty = 0,
            other = 0,
            enrollmentPeriod = enrollmentPeriod
        ).total()

        assertEquals(0, result)
    }

    @Test
    fun calculate_renewedPaying_additionalQualifyingBeneficiariesWithReplacementAndPenalty() {
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(
            enrollmentPeriodId = enrollmentPeriod.id)
        val previousPayment = MembershipPaymentFactory.build(
            annualContributionFee = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE,
            qualifyingBeneficiariesFee = PaymentFees.ANNUAL_BENEFICIARY_FEE,
            penaltyFee = 1000,
            householdEnrollmentRecordId = householdEnrollmentRecord.id
        )
        val members = householdWithTwoQualifyingBeneficiaries.map { MemberWithThumbnail(it, null) }
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(householdEnrollmentRecord),
            members = members,
            payments = listOf(previousPayment)
        )
        val penalty = 1500

        val result = PaymentFees.calculate(
            householdWithMembersAndPayments = householdWithMembersAndPayments,
            isPaying = true,
            isReplacingCard = true,
            isNewEnrollment = false,
            penalty = penalty,
            other = 0,
            enrollmentPeriod = enrollmentPeriod
        ).total()
        val expected = PaymentFees.ANNUAL_BENEFICIARY_FEE + PaymentFees.CARD_FEE + penalty

        assertEquals(expected, result)
    }

    @Test
    fun calculate_renewedPayingOverpaid_noAdditionalQualifyingBeneficiaries() {
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(
            enrollmentPeriodId = enrollmentPeriod.id)
        val previousPayment = MembershipPaymentFactory.build(
            annualContributionFee = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE,
            qualifyingBeneficiariesFee = PaymentFees.ANNUAL_BENEFICIARY_FEE * 4,
            householdEnrollmentRecordId = householdEnrollmentRecord.id
        )
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(householdEnrollmentRecord),
            members = householdWithTwoQualifyingBeneficiaries.map { MemberWithThumbnail(it, null) },
            payments = listOf(previousPayment)
        )

        val result = PaymentFees.calculate(
            householdWithMembersAndPayments = householdWithMembersAndPayments,
            isPaying = true,
            isReplacingCard = false,
            isNewEnrollment = false,
            penalty = 0,
            other = 0,
            enrollmentPeriod = enrollmentPeriod
        ).total()

        assertEquals(0, result)
    }

    @Test
    fun calculate_additionalQualifyingBeneficiaries() {
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(
            enrollmentPeriodId = enrollmentPeriod.id)
        val previousPayment = MembershipPaymentFactory.build(
            annualContributionFee = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE,
            qualifyingBeneficiariesFee = PaymentFees.ANNUAL_BENEFICIARY_FEE * 4,
            householdEnrollmentRecordId = householdEnrollmentRecord.id
        )
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(householdEnrollmentRecord),
            members = householdWithTwoQualifyingBeneficiaries.map { MemberWithThumbnail(it, null) },
            payments = listOf(previousPayment)
        )

        val result = PaymentFees.calculate(
            householdWithMembersAndPayments = householdWithMembersAndPayments,
            isPaying = true,
            isReplacingCard = false,
            isNewEnrollment = false,
            penalty = 0,
            other = 0,
            enrollmentPeriod = enrollmentPeriod
        ).total()

        assertEquals(0, result)
    }

    @Test
    fun calculate_additionalQualifyingBeneficiaries_paidLastPeriod() {
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(
            enrollmentPeriodId = enrollmentPeriod.id)
        val prevPeriodHouseholdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(
            enrollmentPeriodId = 0)
        val currentPeriodPayment = MembershipPaymentFactory.build(
            annualContributionFee = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE,
            qualifyingBeneficiariesFee = PaymentFees.ANNUAL_BENEFICIARY_FEE,
            householdEnrollmentRecordId = householdEnrollmentRecord.id
        )
        val previousPeriodPayment = MembershipPaymentFactory.build(
            annualContributionFee = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE,
            qualifyingBeneficiariesFee = PaymentFees.ANNUAL_BENEFICIARY_FEE * 2,
            householdEnrollmentRecordId = prevPeriodHouseholdEnrollmentRecord.id
        )
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(householdEnrollmentRecord, prevPeriodHouseholdEnrollmentRecord),
            members = householdWithTwoQualifyingBeneficiaries.map { MemberWithThumbnail(it, null) },
            payments = listOf(previousPeriodPayment, currentPeriodPayment)
        )

        val result = PaymentFees.calculate(
            householdWithMembersAndPayments = householdWithMembersAndPayments,
            isPaying = true,
            isReplacingCard = false,
            isNewEnrollment = false,
            penalty = 0,
            other = 0,
            enrollmentPeriod = enrollmentPeriod
        ).total()

        assertEquals(PaymentFees.ANNUAL_BENEFICIARY_FEE, result)
    }
}

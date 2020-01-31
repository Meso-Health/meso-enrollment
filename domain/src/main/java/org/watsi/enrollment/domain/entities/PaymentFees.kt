package org.watsi.enrollment.domain.entities

import org.watsi.enrollment.domain.relations.HouseholdWithMembersAndPayments
import java.io.Serializable

data class PaymentFees(
    val initialEnrollmentFee: Int,
    val annualContributionFee: Int,
    val cardFee: Int,
    val beneficiariesCount: Int,
    val beneficiariesFee: Int,
    val penaltyFee: Int,
    val otherFee: Int
) : Serializable {

    fun total(): Int {
        return intArrayOf(
            initialEnrollmentFee,
            annualContributionFee,
            cardFee,
            beneficiariesFee,
            penaltyFee,
            otherFee
        ).sum()
    }

    companion object {
        const val INITIAL_ENROLLMENT_FEE = 50
        const val ANNUAL_HEAD_OF_HOUSEHOLD_FEE = 150
        const val ANNUAL_BENEFICIARY_FEE = 20
        const val CARD_FEE = 0

        fun calculate(
            householdWithMembersAndPayments: HouseholdWithMembersAndPayments,
            isPaying: Boolean,
            isReplacingCard: Boolean,
            isNewEnrollment: Boolean,
            penalty: Int,
            other: Int,
            enrollmentPeriod: EnrollmentPeriod
        ): PaymentFees {
            if (!isPaying) {
                return PaymentFees(0, 0, 0, 0, 0, 0, 0)
            }
            val activePayingBeneficiariesCount = householdWithMembersAndPayments.members.filter {
                it.member.archivedAt == null && it.member.relationshipToHead != Member.RELATIONSHIP_TO_HEAD_SELF
            }.count()

            val pastPaymentsThisPeriod = householdWithMembersAndPayments.paymentsThisPeriod(enrollmentPeriod)
            val pastContributionFee = pastPaymentsThisPeriod.fold(0) { sum, payment ->
                sum + payment.annualContributionFee
            }
            val pastQualifyingBeneficiaryFees = pastPaymentsThisPeriod.fold(0) { sum, payment ->
                sum + payment.qualifyingBeneficiariesFee
            }
            val pastQualifyingBeneficiaryCount = pastQualifyingBeneficiaryFees / ANNUAL_BENEFICIARY_FEE
            val currentQualifyingBeneficiaryFees = Math.max(0,
                (activePayingBeneficiariesCount * ANNUAL_BENEFICIARY_FEE) - pastQualifyingBeneficiaryFees)

            return PaymentFees(
                initialEnrollmentFee = if (isNewEnrollment) INITIAL_ENROLLMENT_FEE else 0,
                annualContributionFee = Math.max(ANNUAL_HEAD_OF_HOUSEHOLD_FEE - pastContributionFee, 0),
                cardFee = if (isReplacingCard) CARD_FEE else 0,
                beneficiariesCount = Math.max(activePayingBeneficiariesCount - pastQualifyingBeneficiaryCount, 0),
                beneficiariesFee = currentQualifyingBeneficiaryFees,
                penaltyFee = penalty,
                otherFee = other
            )
        }
    }
}

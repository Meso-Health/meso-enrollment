package org.watsi.enrollment.device.api

import org.threeten.bp.LocalDate
import org.watsi.enrollment.domain.entities.MembershipPayment
import java.util.UUID

data class MembershipPaymentApi(
    val id: UUID,
    val receiptNumber: String,
    val paymentDate: LocalDate,
    val annualContributionFee: Int,
    val qualifyingBeneficiariesFee: Int,
    val registrationFee: Int,
    val penaltyFee: Int,
    val cardReplacementFee: Int,
    val otherFee: Int,
    val householdEnrollmentRecordId: UUID
) {
    constructor(membershipPayment: MembershipPayment): this(
        id = membershipPayment.id,
        receiptNumber = membershipPayment.receiptNumber,
        paymentDate = membershipPayment.paymentDate,
        annualContributionFee = membershipPayment.annualContributionFee,
        qualifyingBeneficiariesFee = membershipPayment.qualifyingBeneficiariesFee,
        registrationFee = membershipPayment.registrationFee,
        penaltyFee = membershipPayment.penaltyFee,
        cardReplacementFee = membershipPayment.cardReplacementFee,
        otherFee = membershipPayment.otherFee,
        householdEnrollmentRecordId = membershipPayment.householdEnrollmentRecordId
    )

    fun toMembershipPayment(): MembershipPayment {
        return MembershipPayment(
            id = id,
            receiptNumber = receiptNumber,
            paymentDate = paymentDate,
            annualContributionFee = annualContributionFee,
            qualifyingBeneficiariesFee = qualifyingBeneficiariesFee,
            registrationFee = registrationFee,
            penaltyFee = penaltyFee,
            cardReplacementFee = cardReplacementFee,
            otherFee = otherFee,
            householdEnrollmentRecordId = householdEnrollmentRecordId
        )
    }
}

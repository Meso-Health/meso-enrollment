package org.watsi.enrollment.domain.factories

import org.threeten.bp.LocalDate
import org.watsi.enrollment.domain.entities.MembershipPayment
import java.util.UUID

object MembershipPaymentFactory {
    fun build(
        id: UUID = UUID.randomUUID(),
        receiptNumber: String = "123456",
        paymentDate: LocalDate = LocalDate.now(),
        annualContributionFee: Int = 0,
        qualifyingBeneficiariesFee: Int = 0,
        registrationFee: Int = 0,
        penaltyFee: Int = 0,
        cardReplacementFee: Int = 0,
        otherFee: Int = 0,
        householdEnrollmentRecordId: UUID = UUID.randomUUID()
    ): MembershipPayment {
        return MembershipPayment(
            id = id,
            receiptNumber = receiptNumber,
            paymentDate = paymentDate,
            annualContributionFee = annualContributionFee,
            qualifyingBeneficiariesFee = qualifyingBeneficiariesFee,
            registrationFee = registrationFee,
            cardReplacementFee = cardReplacementFee,
            penaltyFee = penaltyFee,
            otherFee = otherFee,
            householdEnrollmentRecordId = householdEnrollmentRecordId
        )
    }
}

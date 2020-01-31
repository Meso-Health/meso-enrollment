package org.watsi.enrollment.domain.entities

import org.threeten.bp.LocalDate
import java.io.Serializable
import java.util.UUID

data class MembershipPayment(
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
): Serializable

enum class PaymentType {
    NEW_ENROLLMENT, RENEWAL, MANUAL_RENEWAL, ADDITIONAL_PAYMENT
}

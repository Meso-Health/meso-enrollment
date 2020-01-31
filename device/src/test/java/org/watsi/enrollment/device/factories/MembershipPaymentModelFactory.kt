package org.watsi.enrollment.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.enrollment.device.db.daos.MembershipPaymentDao
import org.watsi.enrollment.device.db.models.MembershipPaymentModel
import java.util.UUID

object MembershipPaymentModelFactory {

    fun build(
            id: UUID = UUID.randomUUID(),
            createdAt: Instant? = null,
            updatedAt: Instant? = null,
            receiptNumber: String = "6917352",
            paymentDate: LocalDate = LocalDate.now(),
            annualContributionFee: Int = 200,
            qualifyingBeneficiariesFee: Int = 200,
            registrationFee: Int = 200,
            penaltyFee: Int = 200,
            cardReplacementFee: Int = 200,
            otherFee: Int = 0,
            householdEnrollmentRecordId: UUID,
            clock: Clock = Clock.systemUTC()
    ): MembershipPaymentModel {
        val currentTime = Instant.now(clock)
        return MembershipPaymentModel(
            id = id,
            createdAt = createdAt ?: currentTime,
            updatedAt = updatedAt ?: currentTime,
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

    fun create(
            membershipPaymentDao: MembershipPaymentDao,
            id: UUID = UUID.randomUUID(),
            createdAt: Instant? = null,
            updatedAt: Instant? = null,
            receiptNumber: String = "6917352",
            paymentDate: LocalDate = LocalDate.now(),
            annualContributionFee: Int = 200,
            qualifyingBeneficiariesFee: Int = 200,
            registrationFee: Int = 200,
            penaltyFee: Int = 200,
            cardReplacementFee: Int = 200,
            otherFee: Int = 0,
            householdEnrollmentRecordId: UUID,
            clock: Clock = Clock.systemUTC()
    ): MembershipPaymentModel {
        val paymentModel = build(
            id = id,
            createdAt = createdAt,
            updatedAt = updatedAt,
            receiptNumber = receiptNumber,
            paymentDate = paymentDate,
            annualContributionFee = annualContributionFee,
            qualifyingBeneficiariesFee = qualifyingBeneficiariesFee,
            registrationFee = registrationFee,
            cardReplacementFee = cardReplacementFee,
            penaltyFee = penaltyFee,
            otherFee = otherFee,
            householdEnrollmentRecordId = householdEnrollmentRecordId,
            clock = clock
        )
        membershipPaymentDao.insertWithDelta(paymentModel, emptyList())
        return paymentModel
    }
}

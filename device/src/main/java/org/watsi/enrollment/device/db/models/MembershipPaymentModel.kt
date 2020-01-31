package org.watsi.enrollment.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.enrollment.domain.entities.MembershipPayment
import java.util.UUID

@Entity(tableName = "membershipPayments",
        indices = [
            Index("householdEnrollmentRecordId")
        ],
        foreignKeys = [
            ForeignKey(
                entity = HouseholdEnrollmentRecordModel::class,
                parentColumns = ["id"],
                childColumns = ["householdEnrollmentRecordId"]
            )
        ]
)
data class MembershipPaymentModel(
    @PrimaryKey val id: UUID,
    val receiptNumber: String,
    val paymentDate: LocalDate,
    val annualContributionFee: Int,
    val qualifyingBeneficiariesFee: Int,
    val registrationFee: Int,
    val penaltyFee: Int,
    val cardReplacementFee: Int,
    val otherFee: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val householdEnrollmentRecordId: UUID
){
    fun toMembershipPayment(): MembershipPayment {
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

    companion object {
        fun fromMembershipPayment(membershipPayment: MembershipPayment, clock: Clock): MembershipPaymentModel {
            val now = clock.instant()
            return MembershipPaymentModel(
                id = membershipPayment.id,
                receiptNumber = membershipPayment.receiptNumber,
                paymentDate = membershipPayment.paymentDate,
                annualContributionFee = membershipPayment.annualContributionFee,
                qualifyingBeneficiariesFee = membershipPayment.qualifyingBeneficiariesFee,
                registrationFee = membershipPayment.registrationFee,
                cardReplacementFee = membershipPayment.cardReplacementFee,
                penaltyFee = membershipPayment.penaltyFee,
                otherFee = membershipPayment.otherFee,
                householdEnrollmentRecordId = membershipPayment.householdEnrollmentRecordId,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

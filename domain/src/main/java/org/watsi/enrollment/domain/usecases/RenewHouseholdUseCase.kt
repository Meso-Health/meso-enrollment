package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.HouseholdEnrollmentRecord
import org.watsi.enrollment.domain.entities.MemberEnrollmentRecord
import org.watsi.enrollment.domain.entities.MembershipPayment
import org.watsi.enrollment.domain.entities.User
import org.watsi.enrollment.domain.relations.HouseholdWithMembersAndPayments
import org.watsi.enrollment.domain.repositories.HouseholdEnrollmentRecordRepository
import org.watsi.enrollment.domain.repositories.MemberEnrollmentRecordRepository
import org.watsi.enrollment.domain.repositories.MembershipPaymentRepository
import java.util.UUID

class RenewHouseholdUseCase (
    private val householdEnrollmentRecordRepository: HouseholdEnrollmentRecordRepository,
    private val membershipPaymentRepository: MembershipPaymentRepository,
    private val memberEnrollmentRecordRepository: MemberEnrollmentRecordRepository,
    private val updateMemberUseCase: UpdateMemberUseCase,
    private val loadCurrentEnrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase
) {

    fun execute(
        household: HouseholdWithMembersAndPayments,
        paying: Boolean,
        payment: MembershipPayment?,
        user: User,
        clock: Clock
    ): Completable {
        return loadCurrentEnrollmentPeriodUseCase.executeSingle().flatMapCompletable { enrollmentPeriod ->
            val householdEnrollmentRecord = HouseholdEnrollmentRecord(
                id = payment?.householdEnrollmentRecordId ?: UUID.randomUUID(),
                householdId = household.household.id,
                userId = user.id,
                enrolledAt = Instant.now(clock),
                paying = paying,
                renewal = true,
                administrativeDivisionId = household.household.administrativeDivisionId,
                enrollmentPeriodId = enrollmentPeriod.id
            )

            val householdEnrollmentRecordDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.HOUSEHOLD_ENROLLMENT_RECORD,
                modelId = householdEnrollmentRecord.id
            )

            val householdEnrollmentRecordCompletable = householdEnrollmentRecordRepository.save(
                householdEnrollmentRecord, householdEnrollmentRecordDelta)

            val paymentCompletable = if (payment != null) {
                val paymentDelta = Delta(
                    action = Delta.Action.ADD,
                    modelName = Delta.ModelName.MEMBERSHIP_PAYMENT,
                    modelId = payment.id
                )
                membershipPaymentRepository.save(payment, paymentDelta)
            } else {
                Completable.complete()
            }

            val householdPaymentStatusChanged = (householdEnrollmentRecord.paying != household.wasPayingMostRecentPeriod())

            val updateMembersCompletables = household.members.map { memberWithThumbnail ->
                var updatedMember = memberWithThumbnail.member

                // If the household is switching between P<>I, clear the membership number so that a new
                // one can be generated on the backend
                if (householdPaymentStatusChanged) {
                    updatedMember = updatedMember.copy(membershipNumber = null)

                    // If a household is changing P->I, restore all unpaid beneficiaries
                    if (!householdEnrollmentRecord.paying && updatedMember.unpaid()) {
                        updatedMember = updatedMember.copy(archivedAt = null, archivedReason = null)
                    }
                }

                updateMemberUseCase.execute(updatedMember)
            }

            val memberEnrollmentCompletables = if (householdPaymentStatusChanged) {
                household.members.map { member ->
                    val enrollmentRecord = MemberEnrollmentRecord(
                        id = UUID.randomUUID(),
                        memberId = member.member.id,
                        userId = user.id,
                        enrolledAt = Instant.now(),
                        note = null,
                        enrollmentPeriodId = enrollmentPeriod.id
                    )
                    val enrollmentRecordDelta = Delta(
                        action = Delta.Action.ADD,
                        modelName = Delta.ModelName.MEMBER_ENROLLMENT_RECORD,
                        modelId = enrollmentRecord.id
                    )

                    memberEnrollmentRecordRepository.save(enrollmentRecord, enrollmentRecordDelta)
                }
            } else {
                emptyList()
            }

            // TODO: this should be in a transaction.
            Completable.concatArray(
                householdEnrollmentRecordCompletable,
                paymentCompletable,
                *updateMembersCompletables.toTypedArray(),
                *memberEnrollmentCompletables.toTypedArray()
            )
        }
    }
}

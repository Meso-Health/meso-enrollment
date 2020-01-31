package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.Household
import org.watsi.enrollment.domain.entities.HouseholdEnrollmentRecord
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.MemberEnrollmentRecord
import org.watsi.enrollment.domain.entities.MembershipPayment
import org.watsi.enrollment.domain.entities.User
import org.watsi.enrollment.domain.repositories.HouseholdEnrollmentRecordRepository
import org.watsi.enrollment.domain.repositories.HouseholdRepository
import org.watsi.enrollment.domain.repositories.MemberEnrollmentRecordRepository
import org.watsi.enrollment.domain.repositories.MembershipPaymentRepository
import java.util.UUID

class EnrollHouseholdUseCase(
    private val householdRepository: HouseholdRepository,
    private val householdEnrollmentRecordRepository: HouseholdEnrollmentRecordRepository,
    private val createMemberUseCase: CreateMemberUseCase,
    private val memberEnrollmentRecordRepository: MemberEnrollmentRecordRepository,
    private val membershipPaymentRepository: MembershipPaymentRepository,
    private val loadCurrentEnrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase
) {

    fun execute(
        household: Household,
        paying: Boolean,
        renewal: Boolean,
        members: List<Member>,
        payment: MembershipPayment?,
        user: User
    ): Completable {
        return loadCurrentEnrollmentPeriodUseCase.executeSingle().flatMapCompletable { enrollmentPeriod ->
            val householdEnrollmentRecord = HouseholdEnrollmentRecord(
                id = payment?.householdEnrollmentRecordId ?: UUID.randomUUID(),
                householdId = household.id,
                userId = user.id,
                enrolledAt = household.enrolledAt,
                paying = paying,
                renewal = renewal,
                administrativeDivisionId = household.administrativeDivisionId,
                enrollmentPeriodId = enrollmentPeriod.id
            )

            val householdEnrollmentRecordDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.HOUSEHOLD_ENROLLMENT_RECORD,
                modelId = householdEnrollmentRecord.id
            )

            val householdEnrollmentRecordCompletable = householdEnrollmentRecordRepository.save(
                householdEnrollmentRecord, householdEnrollmentRecordDelta
            )

            val memberCompletables = members.map { createMemberUseCase.execute(it) }

            val memberEnrollmentRecordCompletables = members.map {
                val record = MemberEnrollmentRecord(
                    id = UUID.randomUUID(),
                    memberId = it.id,
                    userId = user.id,
                    enrolledAt = it.enrolledAt,
                    note = null,
                    enrollmentPeriodId = enrollmentPeriod.id
                )
                val delta = Delta(
                    action = Delta.Action.ADD,
                    modelName = Delta.ModelName.MEMBER_ENROLLMENT_RECORD,
                    modelId = record.id
                )
                memberEnrollmentRecordRepository.save(record, delta)
            }

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

            // TODO: this should be in a transaction
            Completable.concatArray(
                householdRepository.save(household),
                householdEnrollmentRecordCompletable,
                *memberCompletables.toTypedArray(),
                *memberEnrollmentRecordCompletables.toTypedArray(),
                paymentCompletable
            )
        }
    }
}

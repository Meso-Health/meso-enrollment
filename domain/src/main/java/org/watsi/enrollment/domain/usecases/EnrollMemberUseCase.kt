package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import org.threeten.bp.Clock
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.EnrollmentPeriod
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.MemberEnrollmentRecord
import org.watsi.enrollment.domain.entities.User
import org.watsi.enrollment.domain.relations.HouseholdWithMembersAndPayments
import org.watsi.enrollment.domain.repositories.MemberEnrollmentRecordRepository
import java.util.UUID

class EnrollMemberUseCase(
    private val memberEnrollmentRecordRepository: MemberEnrollmentRecordRepository,
    private val createMemberUseCase: CreateMemberUseCase
) {
    fun execute(
        member: Member,
        household: HouseholdWithMembersAndPayments,
        enrollmentPeriod: EnrollmentPeriod,
        user: User,
        clock: Clock
    ): Completable {

        val enrollmentRecord = MemberEnrollmentRecord(
            id = UUID.randomUUID(),
            memberId = member.id,
            userId = user.id,
            enrolledAt = member.enrolledAt,
            note = null,
            enrollmentPeriodId = enrollmentPeriod.id
        )
        val enrollmentRecordDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.MEMBER_ENROLLMENT_RECORD,
            modelId = enrollmentRecord.id
        )

        val updatedMember = member.copyAndUpdatePaymentStatus(household, enrollmentPeriod, clock)

        return Completable.concatArray(
            createMemberUseCase.execute(updatedMember),
            memberEnrollmentRecordRepository.save(enrollmentRecord, enrollmentRecordDelta)
        )
    }
}

package org.watsi.enrollment.domain.usecases

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.User
import org.watsi.enrollment.domain.factories.EnrollmentPeriodFactory
import org.watsi.enrollment.domain.factories.HouseholdEnrollmentRecordFactory
import org.watsi.enrollment.domain.factories.HouseholdFactory
import org.watsi.enrollment.domain.factories.HouseholdWithMembersAndPaymentsFactory
import org.watsi.enrollment.domain.factories.MemberFactory
import org.watsi.enrollment.domain.factories.MemberWithThumbnailFactory
import org.watsi.enrollment.domain.factories.MembershipPaymentFactory
import org.watsi.enrollment.domain.factories.UserFactory
import org.watsi.enrollment.domain.repositories.HouseholdEnrollmentRecordRepository
import org.watsi.enrollment.domain.repositories.MemberEnrollmentRecordRepository
import org.watsi.enrollment.domain.repositories.MembershipPaymentRepository

@RunWith(MockitoJUnitRunner::class)
class RenewHouseholdUseCaseTest {

    @Mock lateinit var mockHouseholdEnrollmentRecordRepository: HouseholdEnrollmentRecordRepository
    @Mock lateinit var mockMembershipPaymentRepository: MembershipPaymentRepository
    @Mock lateinit var mockMemberEnrollmentRecordRepository: MemberEnrollmentRecordRepository
    @Mock lateinit var mockUpdateMemberUseCase: UpdateMemberUseCase
    @Mock lateinit var mockLoadCurrentEnrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase

    lateinit var useCase: RenewHouseholdUseCase
    lateinit var user: User
    lateinit var clock: Clock

    @Before
    fun setup() {
        useCase = RenewHouseholdUseCase(
            mockHouseholdEnrollmentRecordRepository,
            mockMembershipPaymentRepository,
            mockMemberEnrollmentRecordRepository,
            mockUpdateMemberUseCase,
            mockLoadCurrentEnrollmentPeriodUseCase
        )

        user = UserFactory.build()
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
        whenever(mockLoadCurrentEnrollmentPeriodUseCase.executeSingle())
                .thenReturn(Single.just(EnrollmentPeriodFactory.build()))
    }

    @Test
    fun execute_noUnpaidMembers_paymentStatusUnchanged() {
        val household = HouseholdFactory.build()
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(
            householdId = household.id,
            paying = true,
            renewal = true,
            enrolledAt = Instant.now(clock)
        )
        val member = MemberFactory.build(
            householdId = household.id
        )
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(householdEnrollmentRecord),
            household = household,
            members = listOf(MemberWithThumbnailFactory.build(member = member))
        )
        val payment = MembershipPaymentFactory.build(
            householdEnrollmentRecordId = householdEnrollmentRecord.id
        )
        val paymentDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.MEMBERSHIP_PAYMENT,
            modelId = payment.id
        )

        val householdEnrollmentRecordDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.HOUSEHOLD_ENROLLMENT_RECORD,
            modelId = householdEnrollmentRecord.id
        )

        whenever(mockHouseholdEnrollmentRecordRepository.save(
            householdEnrollmentRecord, householdEnrollmentRecordDelta)).thenReturn(Completable.complete())
        whenever(mockMembershipPaymentRepository.save(payment, paymentDelta)).thenReturn(Completable
                .complete())
        whenever(mockUpdateMemberUseCase.execute(member)).thenReturn(Completable.complete())

        useCase.execute(householdWithMembersAndPayments, true, payment, user, clock).test().assertComplete()
    }

    @Test
    fun execute_restoreUnpaidMembers_paymentStatusChangedToIndigent() {
        val household = HouseholdFactory.build()
        val prevHouseholdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(
            householdId = household.id, paying = true)
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(
            householdId = household.id,
            paying = false,
            renewal = true,
            enrolledAt = Instant.now(clock)
        )
        val payment = MembershipPaymentFactory.build(
            householdEnrollmentRecordId = householdEnrollmentRecord.id)
        val paymentDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.MEMBERSHIP_PAYMENT,
            modelId = payment.id
        )

        val householdEnrollmentRecordDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.HOUSEHOLD_ENROLLMENT_RECORD,
            modelId = householdEnrollmentRecord.id
        )

        val paidMember = MemberWithThumbnailFactory.build(MemberFactory.build(
            birthdate = LocalDate.of(1975, 5, 5),
            profession = "farmer",
            relationshipToHead = Member.RELATIONSHIP_TO_HEAD_SELF,
            archivedReason = null,
            archivedAt = null
        ))
        val unpaidMember1 = MemberWithThumbnailFactory.build(MemberFactory.build(
            birthdate = LocalDate.of(1980, 5, 5),
            profession = "farmer",
            archivedReason = Member.ARCHIVED_REASON_UNPAID,
            archivedAt = Instant.now()
        ))
        val unpaidMember2 = MemberWithThumbnailFactory.build(MemberFactory.build(
            birthdate = LocalDate.of(1985, 5, 5),
            profession = "farmer",
            archivedReason = Member.ARCHIVED_REASON_UNPAID,
            archivedAt = Instant.now()
        ))

        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            household = household,
            householdEnrollmentRecords = listOf(prevHouseholdEnrollmentRecord),
            members = listOf(
                paidMember,
                unpaidMember1,
                unpaidMember2
            )
        )

        whenever(mockHouseholdEnrollmentRecordRepository.save(
            householdEnrollmentRecord, householdEnrollmentRecordDelta)).thenReturn(Completable.complete())
        whenever(mockMembershipPaymentRepository.save(payment, paymentDelta))
                .thenReturn(Completable.complete())
        whenever(mockUpdateMemberUseCase.execute(clearMembershipNumberAndArchived(paidMember.member)))
                .thenReturn(Completable.complete())
        whenever(mockUpdateMemberUseCase.execute(clearMembershipNumberAndArchived(unpaidMember1.member)))
                .thenReturn(Completable.complete())
        whenever(mockUpdateMemberUseCase.execute(clearMembershipNumberAndArchived(unpaidMember2.member)))
                .thenReturn(Completable.complete())
        whenever(mockMemberEnrollmentRecordRepository.save(any(), any()))
                .thenReturn(Completable.complete())

        useCase.execute(householdWithMembersAndPayments, false, payment, user, clock).test().assertComplete()
    }

    private fun clearMembershipNumberAndArchived(member: Member): Member {
        return member.copy(membershipNumber = null, archivedReason = null, archivedAt = null)
    }
}

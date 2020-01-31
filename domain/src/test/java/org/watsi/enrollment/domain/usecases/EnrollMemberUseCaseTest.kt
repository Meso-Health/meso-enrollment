package org.watsi.enrollment.domain.usecases

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.PaymentFees
import org.watsi.enrollment.domain.entities.User
import org.watsi.enrollment.domain.factories.EnrollmentPeriodFactory
import org.watsi.enrollment.domain.factories.HouseholdEnrollmentRecordFactory
import org.watsi.enrollment.domain.factories.HouseholdFactory
import org.watsi.enrollment.domain.factories.HouseholdWithMembersAndPaymentsFactory
import org.watsi.enrollment.domain.factories.MemberFactory
import org.watsi.enrollment.domain.factories.MemberWithThumbnailFactory
import org.watsi.enrollment.domain.factories.MembershipPaymentFactory
import org.watsi.enrollment.domain.factories.UserFactory
import org.watsi.enrollment.domain.repositories.MemberEnrollmentRecordRepository
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class EnrollMemberUseCaseTest {

    @Mock lateinit var mockMemberEnrollmentRecordRepository: MemberEnrollmentRecordRepository
    @Mock lateinit var mockCreateMemberUseCase: CreateMemberUseCase

    lateinit var useCase: EnrollMemberUseCase
    lateinit var user: User
    lateinit var clock: Clock

    @Before
    fun setup() {
        useCase = EnrollMemberUseCase(
            mockMemberEnrollmentRecordRepository,
            mockCreateMemberUseCase
        )

        user = UserFactory.build()
        clock = Clock.systemDefaultZone()
    }

    @Test
    fun execute_qualifyingMember_isPaying_enrollsMemberNoArchive() {
        val householdId = UUID.randomUUID()
        val householdEnrollmentRecordId = UUID.randomUUID()
        val payment = MembershipPaymentFactory.build(
            annualContributionFee = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE,
            qualifyingBeneficiariesFee = PaymentFees.ANNUAL_BENEFICIARY_FEE * 2,
            householdEnrollmentRecordId = householdEnrollmentRecordId
        )
        val household = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(HouseholdEnrollmentRecordFactory.build(
                householdId = householdId,
                id = householdEnrollmentRecordId,
                paying = true
            )),
            household = HouseholdFactory.build(id = householdId),
            members = listOf(MemberWithThumbnailFactory.build(MemberFactory.build(householdId = householdId))),
            payments = listOf(payment)
        )
        val additionalMember = MemberFactory.build(householdId = householdId)
        val enrollmentPeriod = EnrollmentPeriodFactory.build()

        val argument = argumentCaptor<Member>()
        useCase.execute(additionalMember, household, enrollmentPeriod, user, clock)

        verify(mockMemberEnrollmentRecordRepository).save(any(), any())
        verify(mockCreateMemberUseCase).execute(argument.capture())

        assertNull(argument.firstValue.archivedReason)
        assertNull(argument.firstValue.archivedAt)
    }

    @Test
    fun execute_qualifyingMember_notPaying_enrollsMemberNoArchive() {
        val householdId = UUID.randomUUID()
        val household = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(HouseholdEnrollmentRecordFactory.build(householdId = householdId, paying = false)),
            household = HouseholdFactory.build(id = householdId),
            members = listOf(MemberWithThumbnailFactory.build(MemberFactory.build(householdId = householdId)))
        )
        val member = MemberFactory.build(householdId = householdId, birthdate = LocalDate.of(1985, 1, 1), archivedReason = null)
        val enrollmentPeriod = EnrollmentPeriodFactory.build()

        useCase.execute(member, household, enrollmentPeriod, user, clock)

        verify(mockMemberEnrollmentRecordRepository).save(any(), any())
        verify(mockCreateMemberUseCase).execute(member)
    }

    @Test
    fun execute_qualifyingMember_paying_enrollsMemberAndArchive() {
        val householdId = UUID.randomUUID()
        val household = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(HouseholdEnrollmentRecordFactory.build(householdId = householdId, paying = true)),
            household = HouseholdFactory.build(id = householdId),
            members = listOf(MemberWithThumbnailFactory.build(MemberFactory.build(householdId = householdId)))
        )
        val member = MemberFactory.build(
            householdId = householdId,
            birthdate = LocalDate.of(1985, 1, 1),
            archivedReason = null,
            archivedAt = null
        )
        val argument = argumentCaptor<Member>()
        val enrollmentPeriod = EnrollmentPeriodFactory.build()


        useCase.execute(member, household, enrollmentPeriod, user, clock)

        verify(mockMemberEnrollmentRecordRepository).save(any(), any())
        verify(mockCreateMemberUseCase).execute(argument.capture())

        assertEquals(Member.ARCHIVED_REASON_UNPAID, argument.firstValue.archivedReason)
        assertNotNull(argument.firstValue.archivedAt)
    }

    @Test
    fun execute_qualifyingMember_paying_enrollsMemberAndRestore() {
        val householdId = UUID.randomUUID()
        val householdEnrollmentRecordId = UUID.randomUUID()
        val payment = MembershipPaymentFactory.build(
            annualContributionFee = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE,
            qualifyingBeneficiariesFee = PaymentFees.ANNUAL_BENEFICIARY_FEE * 2,
            householdEnrollmentRecordId = householdEnrollmentRecordId
        )
        val household = HouseholdWithMembersAndPaymentsFactory.build(
            householdEnrollmentRecords = listOf(HouseholdEnrollmentRecordFactory.build(
                householdId = householdId,
                paying = true,
                id = householdEnrollmentRecordId
            )),
            household = HouseholdFactory.build(id = householdId),
            members = listOf(MemberWithThumbnailFactory.build(MemberFactory.build(householdId = householdId))),
            payments = listOf(payment)
        )
        val additionalUnpaidMember = MemberFactory.build(
            householdId = householdId,
            birthdate = LocalDate.of(1985, 1, 1),
            archivedReason = Member.ARCHIVED_REASON_UNPAID,
            archivedAt = Instant.now()
        )
        val argument = argumentCaptor<Member>()
        val enrollmentPeriod = EnrollmentPeriodFactory.build()

        useCase.execute(additionalUnpaidMember, household, enrollmentPeriod, user, clock)

        verify(mockMemberEnrollmentRecordRepository).save(any(), any())
        verify(mockCreateMemberUseCase).execute(argument.capture())

        assertNull(argument.firstValue.archivedReason)
        assertNull(argument.firstValue.archivedAt)
    }
}

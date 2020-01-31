package org.watsi.enrollment.domain.usecases

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.HouseholdEnrollmentRecord
import org.watsi.enrollment.domain.factories.EnrollmentPeriodFactory
import org.watsi.enrollment.domain.factories.HouseholdEnrollmentRecordFactory
import org.watsi.enrollment.domain.factories.HouseholdFactory
import org.watsi.enrollment.domain.factories.MemberFactory
import org.watsi.enrollment.domain.factories.MembershipPaymentFactory
import org.watsi.enrollment.domain.factories.UserFactory
import org.watsi.enrollment.domain.repositories.HouseholdEnrollmentRecordRepository
import org.watsi.enrollment.domain.repositories.HouseholdRepository
import org.watsi.enrollment.domain.repositories.MemberEnrollmentRecordRepository
import org.watsi.enrollment.domain.repositories.MembershipPaymentRepository
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class EnrollHouseholdUseCaseTest {

    @Mock lateinit var mockHouseholdRepository: HouseholdRepository
    @Mock lateinit var mockHouseholdEnrollmentRecordRepository: HouseholdEnrollmentRecordRepository
    @Mock lateinit var mockCreateMemberUseCase: CreateMemberUseCase
    @Mock lateinit var mockMemberEnrollmentRecordRepository: MemberEnrollmentRecordRepository
    @Mock lateinit var mockMembershipPaymentRepository: MembershipPaymentRepository
    @Mock lateinit var mockLoadCurrentEnrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase

    lateinit var useCase: EnrollHouseholdUseCase

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        useCase = EnrollHouseholdUseCase(
            mockHouseholdRepository,
            mockHouseholdEnrollmentRecordRepository,
            mockCreateMemberUseCase,
            mockMemberEnrollmentRecordRepository,
            mockMembershipPaymentRepository,
            mockLoadCurrentEnrollmentPeriodUseCase
        )

        whenever(mockLoadCurrentEnrollmentPeriodUseCase.executeSingle())
                .thenReturn(Single.just(EnrollmentPeriodFactory.build()))
    }

    @Test
    fun execute_success() {
        val household = HouseholdFactory.build()
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(householdId = household.id)
        val member = MemberFactory.build(householdId = household.id)
        val payment = MembershipPaymentFactory.build(householdEnrollmentRecordId = householdEnrollmentRecord.id)
        val user = UserFactory.build()

        val householdEnrollmentRecordDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.HOUSEHOLD_ENROLLMENT_RECORD,
            modelId = householdEnrollmentRecord.id
        )

        whenever(mockHouseholdRepository.save(household)).thenReturn(Completable.complete())
        whenever(mockHouseholdEnrollmentRecordRepository.save(
            any(), eq(householdEnrollmentRecordDelta))).thenReturn(Completable.complete())
        whenever(mockCreateMemberUseCase.execute(any())).thenReturn(Completable
                .complete())
        whenever(mockMemberEnrollmentRecordRepository.save(any(), any())).thenReturn(Completable.complete())
        whenever(mockMembershipPaymentRepository.save(any(), any())).thenReturn(Completable.complete())

        useCase.execute(
            household = household,
            paying = true,
            renewal = false,
            members = listOf(member),
            payment = payment,
            user = user
        ).test().assertComplete()
    }
}

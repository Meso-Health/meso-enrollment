package org.watsi.enrollment.domain.usecases

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.enrollment.domain.repositories.AdministrativeDivisionRepository
import org.watsi.enrollment.domain.repositories.DeltaRepository
import org.watsi.enrollment.domain.repositories.EnrollmentPeriodRepository
import org.watsi.enrollment.domain.repositories.HouseholdEnrollmentRecordRepository
import org.watsi.enrollment.domain.repositories.HouseholdRepository
import org.watsi.enrollment.domain.repositories.MemberEnrollmentRecordRepository
import org.watsi.enrollment.domain.repositories.MemberRepository
import org.watsi.enrollment.domain.repositories.MembershipPaymentRepository

@RunWith(MockitoJUnitRunner::class)
class DeleteUserDataUseCaseTest {
    @Mock lateinit var mockDeltaRepository: DeltaRepository
    @Mock lateinit var mockAdministrativeDivisionRepository: AdministrativeDivisionRepository
    @Mock lateinit var mockHouseholdEnrollmentRecordRepository: HouseholdEnrollmentRecordRepository
    @Mock lateinit var mockMemberEnrollmentRecordRepository: MemberEnrollmentRecordRepository
    @Mock lateinit var mockMembershipPaymentRepository: MembershipPaymentRepository
    @Mock lateinit var mockHouseholdRepository: HouseholdRepository
    @Mock lateinit var mockMemberRepository: MemberRepository
    @Mock lateinit var mockEnrollmentPeriodRepository: EnrollmentPeriodRepository
    lateinit var useCase: DeleteUserDataUseCase

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        useCase = DeleteUserDataUseCase(
            mockDeltaRepository,
            mockAdministrativeDivisionRepository,
            mockHouseholdEnrollmentRecordRepository,
            mockHouseholdRepository,
            mockMemberEnrollmentRecordRepository,
            mockMemberRepository,
            mockMembershipPaymentRepository,
            mockEnrollmentPeriodRepository
        )
    }

    @Test
    fun execute() {
        whenever(mockDeltaRepository.deleteAll())
                .thenReturn(Completable.complete())
        whenever(mockAdministrativeDivisionRepository.deleteAll())
                .thenReturn(Completable.complete())
        whenever(mockHouseholdEnrollmentRecordRepository.deleteAll())
                .thenReturn(Completable.complete())
        whenever(mockMemberEnrollmentRecordRepository.deleteAll())
                .thenReturn(Completable.complete())
        whenever(mockMembershipPaymentRepository.deleteAll())
                .thenReturn(Completable.complete())
        whenever(mockMemberRepository.deleteAll())
                .thenReturn(Completable.complete())
        whenever(mockHouseholdRepository.deleteAll())
                .thenReturn(Completable.complete())
        whenever(mockEnrollmentPeriodRepository.deleteAll())
                .thenReturn(Completable.complete())

        useCase.execute().test().assertComplete()
    }
}

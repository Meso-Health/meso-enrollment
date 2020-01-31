package org.watsi.enrollment.domain.usecases

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.enrollment.domain.entities.EnrollmentPeriod
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.entities.PaymentFees
import org.watsi.enrollment.domain.factories.AdministrativeDivisionFactory
import org.watsi.enrollment.domain.factories.EnrollmentPeriodFactory
import org.watsi.enrollment.domain.repositories.EnrollmentPeriodRepository
import org.watsi.enrollment.domain.repositories.SummaryStatsRepository

@RunWith(MockitoJUnitRunner::class)
class LoadSummaryStatisticsUseCaseTest {

    @Mock lateinit var mockSummaryStatsRepository: SummaryStatsRepository
    @Mock lateinit var mockEnrollmentPeriodRepository: EnrollmentPeriodRepository
    lateinit var useCase: LoadSummaryStatisticsUseCase
    lateinit var filter: LoadSummaryStatisticsUseCase.StatsFilter
    lateinit var enrollmentPeriod: EnrollmentPeriod

    @Before
    fun setup() {
        val adminDivision = AdministrativeDivisionFactory.build()
        filter = LoadSummaryStatisticsUseCase.StatsFilter(
            listOf(adminDivision),
            listOf(true),
            listOf(Gender.M),
            listOf(false)
        )
        useCase = LoadSummaryStatisticsUseCase(mockSummaryStatsRepository, mockEnrollmentPeriodRepository)
        enrollmentPeriod = EnrollmentPeriodFactory.build()
        whenever(mockEnrollmentPeriodRepository.current()).thenReturn(Flowable.just(enrollmentPeriod))
    }

    @Test
    fun execute() {
        val paymentFees = PaymentFees(0, 0, 0, 0, 0, 0, 0)
        whenever(mockSummaryStatsRepository.countEnrolledMembers(filter, enrollmentPeriod.id))
                .thenReturn(Single.just(1))
        whenever(mockSummaryStatsRepository.countEnrolledBeneficiaries(filter, enrollmentPeriod.id))
                .thenReturn(Single.just(2))
        whenever(mockSummaryStatsRepository.totalCollected(filter, enrollmentPeriod.id))
                .thenReturn(Single.just(paymentFees))

        useCase.execute(filter).test().assertValue(Triple(1, 2, paymentFees))
    }
}

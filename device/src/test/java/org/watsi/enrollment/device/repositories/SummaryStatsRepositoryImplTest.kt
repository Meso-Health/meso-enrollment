package org.watsi.enrollment.device.repositories

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.enrollment.device.db.daos.SummaryStatsDao
import org.watsi.enrollment.domain.entities.AdministrativeDivision
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.entities.PaymentFees
import org.watsi.enrollment.domain.factories.AdministrativeDivisionFactory
import org.watsi.enrollment.domain.usecases.LoadSummaryStatisticsUseCase

@RunWith(MockitoJUnitRunner::class)
class SummaryStatsRepositoryImplTest {
    @Mock lateinit var mockSummaryStatsDao: SummaryStatsDao
    lateinit var administrativeDivision: AdministrativeDivision
    lateinit var statsFilter: LoadSummaryStatisticsUseCase.StatsFilter
    lateinit var summaryStatsRepo: SummaryStatsRepositoryImpl
    val enrollmentPeriodId = 5

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        administrativeDivision = AdministrativeDivisionFactory.build()
        statsFilter = LoadSummaryStatisticsUseCase.StatsFilter(
            listOf(administrativeDivision),
            listOf(true),
            listOf(Gender.F),
            listOf(false)
        )
        summaryStatsRepo = SummaryStatsRepositoryImpl(mockSummaryStatsDao)
    }

    @Test
    fun countEnrolledMembers() {
        val result = 1
        whenever(mockSummaryStatsDao.countEnrolled(
            true,
            listOf(administrativeDivision.id),
            statsFilter.payingFilter,
            statsFilter.genderFilter.map { it.toString() },
            statsFilter.renewalFilter,
            enrollmentPeriodId
        )).thenReturn(Single.just(result))

        summaryStatsRepo.countEnrolledMembers(statsFilter, enrollmentPeriodId).test().assertResult(result)
    }

    @Test
    fun countEnrolledBeneficiaries() {
        val result = 1
        whenever(mockSummaryStatsDao.countEnrolled(
            false,
            listOf(administrativeDivision.id),
            statsFilter.payingFilter,
            statsFilter.genderFilter.map { it.toString() },
            statsFilter.renewalFilter,
            enrollmentPeriodId
        )).thenReturn(Single.just(result))

        summaryStatsRepo.countEnrolledBeneficiaries(statsFilter, enrollmentPeriodId).test().assertResult(result)
    }

    @Test
    fun totalCollected() {
        val result = PaymentFees(0, 0, 0 ,0 , 0 , 0 , 0 )
        whenever(mockSummaryStatsDao.totalCollected(
            listOf(administrativeDivision.id),
            statsFilter.payingFilter,
            statsFilter.genderFilter.map { it.toString() },
            statsFilter.renewalFilter,
            enrollmentPeriodId
        )).thenReturn(Single.just(result))

        summaryStatsRepo.totalCollected(statsFilter, enrollmentPeriodId).test().assertResult(result)
    }
}

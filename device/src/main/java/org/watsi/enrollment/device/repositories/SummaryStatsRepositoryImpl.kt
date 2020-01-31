package org.watsi.enrollment.device.repositories

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.watsi.enrollment.device.db.daos.SummaryStatsDao
import org.watsi.enrollment.domain.entities.PaymentFees
import org.watsi.enrollment.domain.repositories.SummaryStatsRepository
import org.watsi.enrollment.domain.usecases.LoadSummaryStatisticsUseCase.StatsFilter

class SummaryStatsRepositoryImpl(
    private val summaryStatsDao: SummaryStatsDao
) : SummaryStatsRepository {
    override fun countEnrolledMembers(filter: StatsFilter, enrollmentPeriodId: Int): Single<Int> {
        val adminDivisionIds = parseadminDivisionIds(filter)
        return summaryStatsDao.countEnrolled(
            isMember = true,
            adminDivisionIds = adminDivisionIds,
            payingFilter = filter.payingFilter,
            genderFilter = filter.genderFilter.map { it.toString() },
            renewalFilter = filter.renewalFilter,
            enrollmentPeriodId = enrollmentPeriodId
        ).subscribeOn(Schedulers.io())
    }

    override fun countEnrolledBeneficiaries(filter: StatsFilter, enrollmentPeriodId: Int): Single<Int> {
        val adminDivisionIds = parseadminDivisionIds(filter)
        return summaryStatsDao.countEnrolled(
            isMember = false,
            adminDivisionIds = adminDivisionIds,
            payingFilter = filter.payingFilter,
            genderFilter = filter.genderFilter.map { it.toString() },
            renewalFilter = filter.renewalFilter,
            enrollmentPeriodId = enrollmentPeriodId
        ).subscribeOn(Schedulers.io())
    }

    override fun totalCollected(filter: StatsFilter, enrollmentPeriodId: Int): Single<PaymentFees> {
        val adminDivisionIds = parseadminDivisionIds(filter)
        return summaryStatsDao.totalCollected(
            adminDivisionIds = adminDivisionIds,
            payingFilter = filter.payingFilter,
            genderFilter = filter.genderFilter.map { it.toString() },
            renewalFilter = filter.renewalFilter,
            enrollmentPeriodId = enrollmentPeriodId
        ).subscribeOn(Schedulers.io())
    }

    private fun parseadminDivisionIds(filter: StatsFilter): List<Int> = filter.adminDivisionFilter.map { it.id }
}

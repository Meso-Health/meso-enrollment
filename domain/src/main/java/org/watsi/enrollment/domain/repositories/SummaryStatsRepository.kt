package org.watsi.enrollment.domain.repositories

import io.reactivex.Single
import org.watsi.enrollment.domain.entities.PaymentFees
import org.watsi.enrollment.domain.usecases.LoadSummaryStatisticsUseCase.StatsFilter

interface SummaryStatsRepository {
    fun countEnrolledMembers(filter: StatsFilter, enrollmentPeriodId: Int): Single<Int>
    fun countEnrolledBeneficiaries(filter: StatsFilter, enrollmentPeriodId: Int): Single<Int>
    fun totalCollected(filter: StatsFilter, enrollmentPeriodId: Int): Single<PaymentFees>
}

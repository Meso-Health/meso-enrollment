package org.watsi.enrollment.domain.usecases

import io.reactivex.Single
import org.watsi.enrollment.domain.entities.AdministrativeDivision
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.entities.PaymentFees
import org.watsi.enrollment.domain.repositories.EnrollmentPeriodRepository
import org.watsi.enrollment.domain.repositories.SummaryStatsRepository

class LoadSummaryStatisticsUseCase(
    private val summaryStatsRepository: SummaryStatsRepository,
    private val enrollmentPeriodRepository: EnrollmentPeriodRepository
) {

    fun execute(filter: StatsFilter): Single<Triple<Int, Int, PaymentFees>> {
        return enrollmentPeriodRepository.current().firstOrError().flatMap { enrollmentPeriod ->
            val singles = listOf(
                summaryStatsRepository.countEnrolledMembers(filter, enrollmentPeriod.id),
                summaryStatsRepository.countEnrolledBeneficiaries(filter, enrollmentPeriod.id),
                summaryStatsRepository.totalCollected(filter, enrollmentPeriod.id)
            )

            Single.zip(singles) {
                Triple(it[0] as Int, it[1] as Int, it[2] as PaymentFees)
            }
        }
    }

    data class StatsFilter(
        var adminDivisionFilter: List<AdministrativeDivision>,
        var payingFilter: List<Boolean>,
        var genderFilter: List<Gender>,
        var renewalFilter: List<Boolean>
    )
}

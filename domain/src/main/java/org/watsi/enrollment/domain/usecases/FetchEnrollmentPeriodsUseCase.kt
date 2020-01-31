package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import org.watsi.enrollment.domain.repositories.EnrollmentPeriodRepository

class FetchEnrollmentPeriodsUseCase(
    private val enrollmentPeriodsRepository: EnrollmentPeriodRepository
) {

    fun execute(): Completable {
        return enrollmentPeriodsRepository.fetch()
    }
}

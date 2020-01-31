package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import org.watsi.enrollment.domain.repositories.EnrollmentPeriodRepository

class VerifyCurrentEnrollmentPeriodExistsUseCase(
    private val enrollmentPeriodRepository: EnrollmentPeriodRepository
) {
    fun execute(): Completable {
        return enrollmentPeriodRepository.current().firstOrError().toCompletable().onErrorResumeNext {
            Completable.error(
                NoEnrollmentPeriodFoundException()
            )
        }
    }

    class NoEnrollmentPeriodFoundException: Exception()
}

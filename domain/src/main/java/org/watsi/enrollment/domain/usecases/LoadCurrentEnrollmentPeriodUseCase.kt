package org.watsi.enrollment.domain.usecases

import io.reactivex.Flowable
import io.reactivex.Single
import org.watsi.enrollment.domain.entities.EnrollmentPeriod
import org.watsi.enrollment.domain.repositories.EnrollmentPeriodRepository

class LoadCurrentEnrollmentPeriodUseCase(
    private val enrollmentPeriodRepository: EnrollmentPeriodRepository
) {

    fun executeFlowable(): Flowable<EnrollmentPeriod> {
        return enrollmentPeriodRepository.current()
    }

    fun executeSingle(): Single<EnrollmentPeriod> {
        return enrollmentPeriodRepository.current().firstOrError()
    }
}

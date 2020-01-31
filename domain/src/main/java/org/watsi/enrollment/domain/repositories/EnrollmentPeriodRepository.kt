package org.watsi.enrollment.domain.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.watsi.enrollment.domain.entities.EnrollmentPeriod

interface EnrollmentPeriodRepository {
    fun current(): Flowable<EnrollmentPeriod>
    fun fetch(): Completable
    fun deleteAll(): Completable
}

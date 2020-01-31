package org.watsi.enrollment.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.enrollment.device.db.daos.EnrollmentPeriodDao
import org.watsi.enrollment.device.db.models.EnrollmentPeriodModel

object EnrollmentPeriodModelFactory {

    fun build(
        id: Int = 0,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate = LocalDate.now().plusYears(1L),
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        clock: Clock = Clock.systemUTC()
    ): EnrollmentPeriodModel {
        val currentTime = Instant.now(clock)
        return EnrollmentPeriodModel(
            id = id,
            startDate = startDate,
            endDate = endDate,
            createdAt = createdAt ?: currentTime,
            updatedAt = updatedAt ?: currentTime
        )
    }

    fun create(
        enrollmentPeriodDao: EnrollmentPeriodDao,
        id: Int = 0,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate = LocalDate.now().plusYears(1L),
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        clock: Clock = Clock.systemUTC()
    ): EnrollmentPeriodModel {
        val enrollmentPeriodModel = build(
            id = id,
            startDate = startDate,
            endDate = endDate,
            createdAt = createdAt,
            updatedAt = updatedAt,
            clock = clock
        )
        enrollmentPeriodDao.upsert(listOf(enrollmentPeriodModel))
        return enrollmentPeriodModel
    }
}

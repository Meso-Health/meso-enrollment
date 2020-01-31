package org.watsi.enrollment.domain.factories

import org.threeten.bp.LocalDate
import org.watsi.enrollment.domain.entities.EnrollmentPeriod

object EnrollmentPeriodFactory {

    fun build(
        id: Int = 1,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate = LocalDate.now().plusYears(1L)
    ): EnrollmentPeriod {
        return EnrollmentPeriod(
            id = id,
            startDate = startDate,
            endDate = endDate
        )
    }
}

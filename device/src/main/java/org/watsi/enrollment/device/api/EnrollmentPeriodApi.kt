package org.watsi.enrollment.device.api

import org.threeten.bp.LocalDate
import org.watsi.enrollment.domain.entities.EnrollmentPeriod

data class EnrollmentPeriodApi(
    val id: Int,
    val startDate: LocalDate,
    val endDate: LocalDate
) {

    constructor (enrollmentPeriod: EnrollmentPeriod) : this(
        id = enrollmentPeriod.id,
        startDate = enrollmentPeriod.startDate,
        endDate = enrollmentPeriod.endDate
    )

    fun toEnrollmentPeriod(): EnrollmentPeriod {
        return EnrollmentPeriod(
            id = id,
            startDate = startDate,
            endDate = endDate
        )
    }
}

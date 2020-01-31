package org.watsi.enrollment.helpers

import android.content.Context
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.enrollment.R
import org.watsi.enrollment.domain.entities.DateAccuracy
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.utils.AgeUnit
import org.watsi.enrollment.domain.utils.DateUtils

object StringHelper {
    fun truncateWithEllipses(qrCode: String, maxCharlength: Int = 25): String {
        return if (maxCharlength >= qrCode.length) {
            qrCode
        } else {
            qrCode.substring(0, maxCharlength) + "..."
        }
    }

    /**
     * Returns quantity in days if under 1 month old, quantity in months if under 2 years old,
     * or in years otherwise, regardless of birthdate accuracy.
     */
    fun getDisplayAge(member: Member, context: Context, clock: Clock = Clock.systemDefaultZone()): String {
        val ageYears = member.getAgeYears(clock)
        val ageMonths = member.getAgeMonths(clock)
        val ageDays = member.getAgeDays(clock)

        if (ageYears >= 2) {
            return "${ageYears} ${context.getString(R.string.years)}"
        } else if (ageMonths >= 1) {
            return "${ageMonths} ${context.getString(R.string.months)}"
        } else {
            return "${ageDays} ${context.getString(R.string.days)}"
        }
    }

    fun formatBirthdate(birthdate: LocalDate, accuracy: DateAccuracy, context: Context): String {
        return when (accuracy) {
            DateAccuracy.Y -> {
                "${DateUtils.getYearsAgo(birthdate)} ${context.getString(R.string.years)}"
            }
            DateAccuracy.M -> {
                "${DateUtils.getMonthsAgo(birthdate)} ${context.getString(R.string.months)}"
            }
            DateAccuracy.D -> {
                DateUtils.formatLocalDate(birthdate)
            }
        }
    }

    fun fromStringToAgeUnit(string: String, context: Context): AgeUnit {
        return when (string) {
            context.getString(R.string.years) -> {
                AgeUnit.years
            }
            context.getString(R.string.months) -> {
                AgeUnit.months
            }
            else -> {
                throw IllegalStateException("AgeUnitPresenter.fromStringToAgeUnit called with invalid string: $string")
            }
        }
    }

    fun getStringNullSafe(resourceId: Int?, context: Context): String? {
        return if (resourceId == null) {
            null
        } else {
            context.getString(resourceId)
        }
    }
}

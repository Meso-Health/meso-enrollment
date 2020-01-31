package org.watsi.enrollment.helpers


import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.IllegalFieldValueException
import org.joda.time.chrono.EthiopicChronology
import org.joda.time.format.DateTimeFormat
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.watsi.enrollment.domain.entities.Member

object EthiopianDateHelper {
    const val DATE_FORMAT = "dd-MM-yyyy"
    const val MONTHS_IN_YEAR = 13

    /**
     * Returns the number of days in the month of the year specified. If the date matches "today"
     * then it will only return the number of days up to today, excluding future days.
     */
    fun daysInMonthNotInFuture(year: Int, month: Int, todayDate: EthiopianDate): Int {
        return if (month == todayDate.month && year == todayDate.year) {
            todayDate.day
        } else {
            DateTime(EthiopicChronology.getInstance())
                    .withDate(year, month, 1) // The dayOfMonth doesn't matter
                    .dayOfMonth().maximumValue
        }
    }

    /**
     * Returns the number of months in the year. If the date matches "today" then it will only
     * return the number of months up to today, excluding future months.
     */
    fun monthsInYearNotInFuture(year: Int, todayDate: EthiopianDate): Int {
        return if (year == todayDate.year) {
            todayDate.month
        } else {
            MONTHS_IN_YEAR
        }
    }

    fun ethiopianDateToInternationalDate(ethiopianDate: EthiopianDate, clock: Clock): LocalDate {
        val internationalDateTime = DateTime(EthiopicChronology.getInstance())
                .withZone(DateTimeZone.forID(clock.zone.id))
                .withDate(ethiopianDate.year, ethiopianDate.month, ethiopianDate.day)
        val internationalInstant = Instant.ofEpochMilli(internationalDateTime.millis)
        return LocalDateTime.ofInstant(internationalInstant, clock.zone).toLocalDate()
    }

    fun ethiopianDateToInstant(ethiopianDate: EthiopianDate, clock: Clock): Instant {
        val ethDateTime = DateTime(EthiopicChronology.getInstance())
                .withZone(DateTimeZone.forID(clock.zone.id))
                .withDate(ethiopianDate.year, ethiopianDate.month, ethiopianDate.day)

        return Instant.ofEpochMilli(ethDateTime.millis)
    }

    fun instantToEthiopianDate(instant: Instant, clock: Clock): EthiopianDate {
        val ethDate = DateTime(EthiopicChronology.getInstance())
                .withZone(DateTimeZone.forID(clock.zone.id))
                .withMillis(instant.toEpochMilli())
        return EthiopianDate(ethDate.year, ethDate.monthOfYear, ethDate.dayOfMonth)
    }

    fun instantToFormattedEthiopianDate(instant: Instant, clock: Clock): String {
        val dateTime = DateTime(EthiopicChronology.getInstance())
                .withZone(DateTimeZone.forID(clock.zone.id))
                .withMillis(instant.toEpochMilli())
        return DateTimeFormat.forPattern(DATE_FORMAT).print(dateTime)
    }

    fun internationalDateToEthiopianDate(localDate: LocalDate, clock: Clock): EthiopianDate {
        val instant = localDate.atStartOfDay(clock.zone).toInstant()
        val dateTime = DateTime(EthiopicChronology.getInstance())
                .withZone(DateTimeZone.forID(clock.zone.id))
                .withMillis(instant.toEpochMilli())
        return EthiopianDate(dateTime.year, dateTime.monthOfYear, dateTime.dayOfMonth)
    }

    fun internationalDateToFormattedEthiopianDate(localDate: LocalDate, clock: Clock): String {
        val instant = localDate.atStartOfDay(clock.zone).toInstant()
        val dateTime = DateTime(EthiopicChronology.getInstance())
                .withZone(DateTimeZone.forID(clock.zone.id))
                .withMillis(instant.toEpochMilli())
        return DateTimeFormat.forPattern(DATE_FORMAT).print(dateTime)
    }

    fun isValidEthiopianBirthdate(ethYear: Int, ethMonth: Int, ethDay: Int, clock: Clock): Boolean {
        return try {
            val ethDate = DateTime(EthiopicChronology.getInstance())
                    .withZone(DateTimeZone.forID(clock.zone.id))
                    .withDate(ethYear, ethMonth, ethDay)
            val ethDateNow = DateTime(EthiopicChronology.getInstance())
                    .withZone(DateTimeZone.forID(clock.zone.id))
            ethDate.isBeforeNow && ethYear >= (ethDateNow.year - Member.MAX_AGE)
        } catch (e: IllegalFieldValueException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    data class EthiopianDate(val year: Int, val month: Int, val day: Int)
}

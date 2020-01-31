package org.watsi.enrollment.helpers

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.joda.time.IllegalFieldValueException
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.watsi.enrollment.helpers.EthiopianDateHelper.EthiopianDate
import org.watsi.enrollment.helpers.EthiopianDateHelper.ethiopianDateToInternationalDate
import org.watsi.enrollment.helpers.EthiopianDateHelper.internationalDateToEthiopianDate

class EthiopianDateHelperTest {
    lateinit var clock: Clock

    @Before
    fun setup() {
        clock = Clock.fixed(Instant.now(), ZoneId.of("Africa/Addis_Ababa"))
    }

    @Test
    fun internationalDateToFormattedEthiopianDate() {
        val testDate = LocalDate.parse("2018-09-10")
        val expectedString = "05-13-2010"

        assertEquals(expectedString, EthiopianDateHelper.internationalDateToFormattedEthiopianDate(testDate, clock))
    }

    @Test
    fun daysInMonthNotInFuture() {
        val fullMonth = 3
        val intercalaryMonth = 13
        val thisMonth = 9
        val nonLeapYear = 2009
        val leapYear = 2007
        val thisYear = 2010
        val expectedFullMonth = 30
        val expectedIntercalaryMonth = 5
        val expectedIntercalaryLeapYear = 6
        val expectedThisMonth = 13
        val todayDate = EthiopianDate(thisYear, thisMonth, expectedThisMonth)

        assertEquals(expectedFullMonth,
            EthiopianDateHelper.daysInMonthNotInFuture(nonLeapYear, fullMonth, todayDate))
        assertEquals(expectedIntercalaryMonth,
            EthiopianDateHelper.daysInMonthNotInFuture(nonLeapYear, intercalaryMonth, todayDate))
        assertEquals(expectedIntercalaryLeapYear,
            EthiopianDateHelper.daysInMonthNotInFuture(leapYear, intercalaryMonth, todayDate))
        assertEquals(expectedThisMonth,
            EthiopianDateHelper.daysInMonthNotInFuture(thisYear, thisMonth, todayDate))
    }

    @Test
    fun monthsInYearNotInFuture() {
        val pastYear = 2009
        val thisYear = 2010
        val expectedPastYear = 13
        val expectedThisYear = 9
        val todayDate = EthiopianDate(thisYear, expectedThisYear, 1)

        assertEquals(expectedPastYear,
            EthiopianDateHelper.monthsInYearNotInFuture(pastYear, todayDate))
        assertEquals(expectedThisYear,
            EthiopianDateHelper.monthsInYearNotInFuture(thisYear, todayDate))
    }

    @Test
    fun ethiopianDateToInternationalDate_valid() {
        assertEquals(
            ethiopianDateToInternationalDate(EthiopianDate(2010, 9, 13), clock),
            LocalDate.parse("2018-05-21")
        )
        assertEquals(
            ethiopianDateToInternationalDate(EthiopianDate(2010, 13, 5), clock),
            LocalDate.parse("2018-09-10")
        )
        assertEquals(
            ethiopianDateToInternationalDate(EthiopianDate(2011, 1, 1), clock),
            LocalDate.parse("2018-09-11")
        )
        assertEquals(
            ethiopianDateToInternationalDate(EthiopianDate(2012, 6, 21), clock),
            LocalDate.parse("2020-02-29")
        )
        assertEquals(
            ethiopianDateToInternationalDate(EthiopianDate(1995, 4, 22), clock),
            LocalDate.parse("2002-12-31")
        )
    }

    @Test(expected = IllegalFieldValueException::class)
    fun ethiopianDateToInternationalDate_invalidDay() {
        val invalidEthDate = EthiopianDate(2010, 12, 31)
        EthiopianDateHelper.ethiopianDateToInternationalDate(invalidEthDate, clock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun ethiopianDateToInternationalDate_invalidMonth() {
        val invalidEthDate = EthiopianDate(0, 12, 30)
        EthiopianDateHelper.ethiopianDateToInternationalDate(invalidEthDate, clock)
    }

    @Test
    fun internationalDateToEthiopianDate_valid() {
        assertEquals(
            internationalDateToEthiopianDate(LocalDate.parse("2018-05-21"), clock),
            EthiopianDate(2010, 9, 13)
        )
        assertEquals(
            internationalDateToEthiopianDate(LocalDate.parse("2018-09-10"), clock),
            EthiopianDate(2010, 13, 5)
        )
        assertEquals(
            internationalDateToEthiopianDate(LocalDate.parse("2018-09-11"), clock),
            EthiopianDate(2011, 1, 1)
        )
        assertEquals(
            internationalDateToEthiopianDate(LocalDate.parse("2020-02-29"), clock),
            EthiopianDate(2012, 6, 21)
        )
        assertEquals(
            internationalDateToEthiopianDate(LocalDate.parse("2002-12-31"), clock),
            EthiopianDate(1995, 4, 22)
        )
    }

    @Test
    fun isValidEthiopianBirthdate() {
        assertTrue(EthiopianDateHelper.isValidEthiopianBirthdate(2009, 10, 1, clock))
        assertFalse(EthiopianDateHelper.isValidEthiopianBirthdate(2008, 14, 1, clock))
        assertTrue(EthiopianDateHelper.isValidEthiopianBirthdate(2008, 13, 1, clock))
        assertFalse(EthiopianDateHelper.isValidEthiopianBirthdate(2008, 12, 31, clock))
        assertTrue(EthiopianDateHelper.isValidEthiopianBirthdate(2008, 12, 30, clock))

        val tomorrow = LocalDate.now(clock).plusDays(1)
        assertFalse(EthiopianDateHelper.isValidEthiopianBirthdate(tomorrow.year, tomorrow.monthValue, tomorrow.dayOfMonth, clock))

        val tenDaysLater = LocalDate.now(clock).plusDays(10)
        assertFalse(EthiopianDateHelper.isValidEthiopianBirthdate(tenDaysLater.year, tenDaysLater.monthValue, tenDaysLater.dayOfMonth, clock))

        val nextMonth = LocalDate.now(clock).plusMonths(1)
        assertFalse(EthiopianDateHelper.isValidEthiopianBirthdate(nextMonth.year, nextMonth.monthValue, nextMonth.dayOfMonth, clock))

        val nextYear = LocalDate.now(clock).plusMonths(1)
        assertFalse(EthiopianDateHelper.isValidEthiopianBirthdate(nextYear.year, nextYear.monthValue, nextYear.dayOfMonth, clock))

        val uhcYear = LocalDate.of(2030, 1, 1)
        assertFalse(EthiopianDateHelper.isValidEthiopianBirthdate(uhcYear.year, uhcYear.monthValue, uhcYear.dayOfMonth, clock))

        val tooOld = LocalDate.of(1800, 1, 1)
        assertFalse(EthiopianDateHelper.isValidEthiopianBirthdate(tooOld.year, tooOld.monthValue, tooOld.dayOfMonth, clock))
    }
}

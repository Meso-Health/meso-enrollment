package org.watsi.enrollment.domain.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.watsi.enrollment.domain.entities.DateAccuracy

@RunWith(MockitoJUnitRunner::class)
class DateUtilsTest {
    val zone = ZoneId.systemDefault()
    val dateTime = ZonedDateTime.of(2018, 2, 28, 23, 30, 0, 0, zone)
    val instant = dateTime.toInstant()
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

    @Test
    fun formatLocalDate() {
        val date = LocalDate.of(1993, 7, 2)
        assertEquals(DateUtils.formatLocalDate(date), "02-07-1993")
    }

    @Test
    fun isDateInFuture() {
        val yesterday = LocalDate.now(clock).minusDays(1)
        val today = LocalDate.now(clock)
        val tomorrow = LocalDate.now(clock).plusDays(1)

        assertEquals(DateUtils.isDateInFuture(yesterday), false)
        assertEquals(DateUtils.isDateInFuture(today), false)
        assertEquals(DateUtils.isDateInFuture(tomorrow), true)
    }

    @Test
    fun dateWithAccuracyToAge() {
        val oneYearAgo = LocalDate.now(clock).minusMonths(16)

        assertEquals(
                DateUtils.dateWithAccuracyToAge(oneYearAgo, DateAccuracy.Y, clock),
                Age(1, AgeUnit.years)
        )

        assertEquals(
                DateUtils.dateWithAccuracyToAge(oneYearAgo, DateAccuracy.M, clock),
                Age(16, AgeUnit.months)
        )

        assertEquals(
                DateUtils.dateWithAccuracyToAge(oneYearAgo, DateAccuracy.D, clock),
                null
        )
    }

    @Test
    fun isValidBirthdate() {
        assertEquals(DateUtils.isValidBirthdate(1990, 1, 0, clock), false)
        assertEquals(DateUtils.isValidBirthdate(1990, 13, 1, clock), false)
        assertEquals(DateUtils.isValidBirthdate(1800, 1, 1, clock), false)
        assertEquals(DateUtils.isValidBirthdate(3000, 1, 1, clock), false)
        assertEquals(DateUtils.isValidBirthdate(2001, 2, 29, clock), false)
        assertEquals(DateUtils.isValidBirthdate(1990, 1, 1, clock), true)
    }

    @Test
    fun getSecondsAgo() {
        val now = Instant.now(clock)
        val tenSecondsAgo = now.minusSeconds(10)
        assertTrue(DateUtils.getSecondsAgo(tenSecondsAgo.toEpochMilli(), clock) == 10.0)
    }

    @Test
    fun Age_toBirthdateWithAccuracy() {
        val zeroMonths = Age(0, AgeUnit.months)
        val zeroYears = Age(0, AgeUnit.years)
        val eightMonths = Age(8, AgeUnit.months)
        val tenYears = Age(10, AgeUnit.years)

        assertEquals(
                zeroMonths.toBirthdateWithAccuracy(clock),
                Pair(LocalDate.now(clock), DateAccuracy.M)
        )

        assertEquals(
                zeroYears.toBirthdateWithAccuracy(clock),
                Pair(LocalDate.now(clock), DateAccuracy.Y)
        )

        assertEquals(
                eightMonths.toBirthdateWithAccuracy(clock),
                Pair(LocalDate.now(clock).minusMonths(8), DateAccuracy.M)
        )

        assertEquals(
                tenYears.toBirthdateWithAccuracy(clock),
                Pair(LocalDate.now(clock).minusYears(10), DateAccuracy.Y)
        )
    }
}

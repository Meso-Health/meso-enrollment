package org.watsi.enrollment.helpers

import android.content.Context
import com.nhaarman.mockito_kotlin.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.watsi.enrollment.R
import org.watsi.enrollment.domain.factories.MemberFactory
import org.watsi.enrollment.helpers.StringHelper.truncateWithEllipses

@RunWith(MockitoJUnitRunner::class)
class StringHelperTest {
    @Mock
    private lateinit var mockContext: Context

    val now = Instant.now()
    val fixedClock = Clock.fixed(now, ZoneId.systemDefault())

    @Before
    fun setup() {
        whenever(mockContext.getString(R.string.years)).thenReturn("years")
        whenever(mockContext.getString(R.string.months)).thenReturn("months")
    }

    @Test
    fun getDisplayAge_lessThanTwoYears_returnsMonths() {
        val birthdate = LocalDate.now(fixedClock).minusMonths(15)
        val member = MemberFactory.build(birthdate = birthdate)

        Assert.assertEquals(StringHelper.getDisplayAge(member, mockContext, fixedClock), "15 months")
    }

    @Test
    fun getDisplayAge_isTwoYears_returnsYears() {
        val birthdate = LocalDate.now(fixedClock).minusMonths(24)
        val member = MemberFactory.build(birthdate = birthdate)

        Assert.assertEquals(StringHelper.getDisplayAge(member, mockContext, fixedClock), "2 years")
    }

    @Test
    fun getDisplayAge_overTwoYears_returnsFloorYears() {
        val birthdate = LocalDate.now(fixedClock).minusMonths(30)
        val member = MemberFactory.build(birthdate = birthdate)

        Assert.assertEquals(StringHelper.getDisplayAge(member, mockContext, fixedClock), "2 years")
    }

    @Test
    fun truncateWithEllipses_lessThanThreshold() {
        assertEquals(truncateWithEllipses("hey", 5), "hey")
    }

    @Test
    fun truncateWithEllipses_equalsThreshold() {
        assertEquals(truncateWithEllipses("12345", 5), "12345")
    }

    @Test
    fun truncateWithEllipses_moreThanThreshold() {
        assertEquals(truncateWithEllipses("123456", 5), "12345...")
    }
}

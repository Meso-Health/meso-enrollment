package org.watsi.enrollment.device.managers

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.enrollment.device.testutils.AACBaseTest

/**
 * Contains pure unit tests that cover basic start/stop functionality
 *
 * Higher level integration behavior will be tested in a device test
 */
@RunWith(MockitoJUnitRunner::class)
class AndroidGpsManagerTest : AACBaseTest() {
    @Mock lateinit var mockLocationClient: FusedLocationProviderClient
    @Mock lateinit var mockLocation: Location
    lateinit var gpsManager: AndroidGpsManager

    @Before
    fun setup() {
        gpsManager = AndroidGpsManager(mockLocationClient)
    }

    @Test
    fun setsUpAnInitialGpsState() {
        assertNotNull(gpsManager.gpsState)
        assertNull(gpsManager.gpsState.location)
        assertEquals(GpsManager.GpsStatus.NOT_STARTED, gpsManager.gpsState.status)
    }

    @Test
    fun start_isStarted_doesNotDoAnything() {
        gpsManager.gpsState = GpsManager.GpsState(
                status = GpsManager.GpsStatus.STARTED, location = null)

        gpsManager.start()
    }

    @Test
    fun start_isNotStartedAndDoesNotHavePermission_setsInsufficientPermissionsStatus() {
        gpsManager.gpsState = GpsManager.GpsState(
                status = GpsManager.GpsStatus.NOT_STARTED, location = null)
        whenever(mockLocationClient.requestLocationUpdates(any(), any(), eq(null)))
                .thenThrow(SecurityException())

        gpsManager.start()

        assertEquals(GpsManager.GpsStatus.INSUFFICIENT_PERMISSIONS, gpsManager.gpsState.status)
    }

    @Test
    fun start_isNotStartedAndHasPermission_startsRequestingLocationUpdates() {
        gpsManager.gpsState = GpsManager.GpsState(
                status = GpsManager.GpsStatus.NOT_STARTED, location = null)

        gpsManager.start()

        verify(mockLocationClient).requestLocationUpdates(any(), any(), eq(null))
        assertEquals(GpsManager.GpsStatus.STARTED, gpsManager.gpsState.status)
    }

    @Test
    fun stop_isStarted_stopsRequestingLocationUpdatesAndNulls() {
        gpsManager.gpsState = GpsManager.GpsState(
                status = GpsManager.GpsStatus.STARTED, location = null)

        gpsManager.stop()

        verify(mockLocationClient).removeLocationUpdates(any<LocationCallback>())
        assertEquals(GpsManager.GpsStatus.STOPPED, gpsManager.gpsState.status)
    }

    @Test
    fun stop_isNotStarted_doesNotRemoveTheLocationCallback() {
        gpsManager.gpsState = GpsManager.GpsState(
                status = GpsManager.GpsStatus.NOT_STARTED, location = null)

        gpsManager.stop()

        verifyNoMoreInteractions(mockLocationClient)
        assertEquals(GpsManager.GpsStatus.NOT_STARTED, gpsManager.gpsState.status)
    }

    @Test
    fun isTolerableLocation() {
        assertFalse(AndroidGpsManager.isTolerableLocation(null))
        assertFalse(AndroidGpsManager.isTolerableLocation(createMockLocation(false, 50.0f, 5)))
        assertFalse(AndroidGpsManager.isTolerableLocation(createMockLocation(true, 140.0f, 5)))
        assertFalse(AndroidGpsManager.isTolerableLocation(createMockLocation(true, 40.0f, 35)))
        assertTrue(AndroidGpsManager.isTolerableLocation(createMockLocation(true, 30.0f, 5)))
    }

    private fun createMockLocation(hasAccuracy: Boolean, accuracy: Float, secondsAgo: Long): Location {
        whenever(mockLocation.hasAccuracy()).thenReturn(hasAccuracy)
        whenever(mockLocation.accuracy).thenReturn(accuracy)
        val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
        whenever(mockLocation.time).thenReturn(Instant.now(clock).minusSeconds(secondsAgo).toEpochMilli())
        return mockLocation
    }
}

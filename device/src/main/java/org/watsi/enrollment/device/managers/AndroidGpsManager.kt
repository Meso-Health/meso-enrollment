package org.watsi.enrollment.device.managers

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import org.threeten.bp.Clock
import org.watsi.enrollment.domain.utils.DateUtils

class AndroidGpsManager (
        private val gpsClient: FusedLocationProviderClient,
        private val locationRequest: LocationRequest = generateLocationRequest()
) : GpsManager {

    companion object {
        private const val GPS_READING_INTERVAL: Long = 5 * 1000 // 5 seconds
        private const val TOLERABLE_GPS_ACCURACY_IN_METERS = 100
        private const val TOLERABLE_TIME_SINCE_LAST_GPS_READING_IN_SECONDS = 30

        private fun generateLocationRequest(): LocationRequest {
            return LocationRequest().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = GPS_READING_INTERVAL
                fastestInterval = GPS_READING_INTERVAL
            }
        }

        fun isTolerableLocation(location: Location?, clock: Clock = Clock.systemDefaultZone()): Boolean {
            return location != null
                    && location.hasAccuracy()
                    && location.accuracy < TOLERABLE_GPS_ACCURACY_IN_METERS
                    && DateUtils.getSecondsAgo(location.time, clock) < TOLERABLE_TIME_SINCE_LAST_GPS_READING_IN_SECONDS
        }
    }

    private val callback = GpsLocationCallback()
    internal var gpsState = GpsManager.GpsState(GpsManager.GpsStatus.NOT_STARTED, null)
    internal var gpsStateObservable = PublishSubject.create<GpsManager.GpsState>()

    fun updateCurrentState(gpsState: GpsManager.GpsState) {
        gpsStateObservable.onNext(gpsState)
        this.gpsState = gpsState
    }

    override fun start() {
        if (gpsState.status == GpsManager.GpsStatus.STARTED) return

        try {
            gpsClient.requestLocationUpdates(locationRequest, callback, null)
            updateCurrentState(gpsState.copy(status = GpsManager.GpsStatus.STARTED))
        } catch (e: SecurityException) {
            updateCurrentState(gpsState.copy(status = GpsManager.GpsStatus.INSUFFICIENT_PERMISSIONS))
        }
    }

    override fun stop() {
        if (gpsState.status != GpsManager.GpsStatus.STARTED) return

        gpsClient.removeLocationUpdates(callback)
        gpsState = gpsState.copy(status = GpsManager.GpsStatus.STOPPED)
    }

    override fun getCurrentStateFlowable(): Flowable<GpsManager.GpsState> {
        return gpsStateObservable
                .toFlowable(BackpressureStrategy.BUFFER)
                .startWith(gpsState)
                .filter { isTolerableLocation(it.location) }
    }

    inner class GpsLocationCallback : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult?.lastLocation?.hasAccuracy() == true) {
                val location = locationResult.lastLocation
                updateCurrentState(gpsState.copy(location = location))
            }
        }
    }
}

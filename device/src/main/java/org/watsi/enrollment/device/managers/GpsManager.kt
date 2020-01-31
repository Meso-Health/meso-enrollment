package org.watsi.enrollment.device.managers

import android.location.Location
import io.reactivex.Flowable

interface GpsManager {
    fun start()
    fun stop()
    fun getCurrentStateFlowable(): Flowable<GpsState>

    data class GpsState(val status: GpsStatus, val location: Location?)
    enum class GpsStatus { NOT_STARTED, STARTED, STOPPED, INSUFFICIENT_PERMISSIONS }
}

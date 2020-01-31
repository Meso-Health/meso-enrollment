package org.watsi.enrollment.device.managers

import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.AuthenticationToken
import org.watsi.enrollment.domain.entities.User
import java.util.Locale

interface PreferencesManager {
    fun getAuthenticationToken(): AuthenticationToken?
    fun setAuthenticationToken(token: AuthenticationToken?)
    fun getPreviousUser(): User?
    fun setPreviousUser(user: User?)
    fun getLocale(): Locale?
    fun updateLocale(locale: Locale)
    fun getLastFetched(): Instant
    fun updateLastFetched(instant: Instant)
    fun getDataLastSynced(): Instant
    fun updateDataLastSynced(instant: Instant)
    fun getPhotoLastSynced(): Instant
    fun updatePhotoLastSynced(instant: Instant)
    fun getAdministrativeDivisionsLastFetched(): Instant
    fun updateAdministrativeDivisionsLastFetched(instant: Instant)
    fun getHouseholdsLastFetched(): Instant
    fun updateHouseholdsLastFetched(instant: Instant)
    fun getMemberPhotosLastFetched(): Instant
    fun updateMemberPhotosLastFetched(instant: Instant)
}

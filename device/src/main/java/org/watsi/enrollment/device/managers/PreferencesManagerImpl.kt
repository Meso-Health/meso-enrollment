package org.watsi.enrollment.device.managers

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson
import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.AuthenticationToken
import org.watsi.enrollment.domain.entities.User
import java.util.Locale

class PreferencesManagerImpl(context: Context, private val gson: Gson = Gson()) : PreferencesManager {

    companion object {
        private const val AUTHENTICATION_TOKEN_KEY = "authentication_token"
        private const val LOCALE_KEY = "locale"
        private const val USER = "user"
        private const val LAST_FETCHED_KEY = "last_fetched"
        private const val DATA_LAST_SYNCED_KEY = "data_last_synced"
        private const val PHOTOS_LAST_SYNCED_KEY = "photos_last_synced"
        private const val ADMINISTRATIVE_DIVISIONS_LAST_FETCHED_KEY = "administrative_divisions_last_fetched"
        private const val HOUSEHOLDS_LAST_FETCHED_KEY = "households_last_fetched"
        private const val MEMBER_PHOTOS_LAST_FETCHED_KEY = "member_photos_last_fetched"
    }

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun getAuthenticationToken(): AuthenticationToken? {
        val tokenJson = sharedPreferences.getString(AUTHENTICATION_TOKEN_KEY, null)
        return if (tokenJson == null) null else gson.fromJson(tokenJson, AuthenticationToken::class.java)
    }

    override fun setAuthenticationToken(token: AuthenticationToken?) {
        val tokenJson = if (token == null) null else gson.toJson(token)
        sharedPreferences.edit().putString(AUTHENTICATION_TOKEN_KEY, tokenJson).apply()
    }

    override fun getPreviousUser(): User? {
        val userJson = sharedPreferences.getString(USER, null)
        return if (userJson == null) null else gson.fromJson(userJson, User::class.java)
    }

    override fun setPreviousUser(user: User?) {
        val userJson = if (user == null) null else gson.toJson(user)
        sharedPreferences.edit().putString(USER, userJson).apply()
    }

    override fun getLocale(): Locale? {
        val localeString = sharedPreferences.getString(LOCALE_KEY, null)
        if (localeString == null) {
            return localeString
        }
        val localeParts = localeString.split("_".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val language = localeParts[0]
        val country = if (localeParts.size > 1) localeParts[1] else ""
        return Locale(language, country)
    }

    override fun updateLocale(locale: Locale) {
        sharedPreferences.edit().putString(LOCALE_KEY, locale.toString()).apply()
    }

    override fun getLastFetched(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(LAST_FETCHED_KEY, 0))
    }

    override fun updateLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
    }

    override fun getDataLastSynced(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(DATA_LAST_SYNCED_KEY, 0))
    }

    override fun updateDataLastSynced(instant: Instant) {
        sharedPreferences.edit().putLong(DATA_LAST_SYNCED_KEY, instant.toEpochMilli()).apply()
    }

    override fun getPhotoLastSynced(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(PHOTOS_LAST_SYNCED_KEY, 0))
    }

    override fun updatePhotoLastSynced(instant: Instant) {
        sharedPreferences.edit().putLong(PHOTOS_LAST_SYNCED_KEY, instant.toEpochMilli()).apply()
    }

    override fun getAdministrativeDivisionsLastFetched(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(ADMINISTRATIVE_DIVISIONS_LAST_FETCHED_KEY, 0))
    }

    override fun updateAdministrativeDivisionsLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(ADMINISTRATIVE_DIVISIONS_LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
    }

    override fun getHouseholdsLastFetched(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(HOUSEHOLDS_LAST_FETCHED_KEY, 0))
    }

    override fun updateHouseholdsLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(HOUSEHOLDS_LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
    }

    override fun getMemberPhotosLastFetched(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(MEMBER_PHOTOS_LAST_FETCHED_KEY, 0))
    }

    override fun updateMemberPhotosLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(MEMBER_PHOTOS_LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
    }
}

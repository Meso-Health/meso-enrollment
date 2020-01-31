package org.watsi.enrollment.di.modules

import android.content.Context
import com.google.android.gms.location.LocationServices
import com.rollbar.android.Rollbar
import dagger.Module
import dagger.Provides
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.device.api.CoverageApi
import org.watsi.enrollment.device.managers.AndroidGpsManager
import org.watsi.enrollment.device.managers.GpsManager
import org.watsi.enrollment.device.managers.IntentHelper
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.device.managers.NetworkManager
import org.watsi.enrollment.device.managers.NetworkManagerImpl
import org.watsi.enrollment.device.managers.PreferencesManager
import org.watsi.enrollment.device.managers.PreferencesManagerImpl
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.device.managers.SessionManagerImpl
import org.watsi.enrollment.managers.AndroidKeyboardManager
import org.watsi.enrollment.managers.DebugLogger
import org.watsi.enrollment.managers.KeyboardManager
import org.watsi.enrollment.managers.RollbarLogger
import javax.inject.Singleton

@Module
class DeviceModule {

    @Singleton
    @Provides
    fun providePreferencesManager(context: Context): PreferencesManager = PreferencesManagerImpl(context)

    @Singleton
    @Provides
    fun provideLogger(context: Context): Logger {
        return if (BuildConfig.REPORT_TO_ROLLBAR) {
            val rollbarEnvironmentIdentifier = BuildConfig.FLAVOR + BuildConfig.BUILD_TYPE
            Rollbar.init(context, BuildConfig.ROLLBAR_API_KEY, rollbarEnvironmentIdentifier)
            RollbarLogger()
        } else {
            DebugLogger()
        }
    }

    @Singleton
    @Provides
    fun provideNetworkManager(context: Context): NetworkManager {
        return NetworkManagerImpl(context)
    }

    @Singleton
    @Provides
    fun provideSessionManager(preferencesManager: PreferencesManager, api: CoverageApi, logger: Logger): SessionManager {
        return SessionManagerImpl(preferencesManager, api, logger)
    }

    @Singleton
    @Provides
    fun provideIntentHelper(): IntentHelper = IntentHelper()

    @Provides
    fun provideKeyboardManager(context: Context): KeyboardManager {
        return AndroidKeyboardManager(context)
    }

    @Singleton
    @Provides
    fun provideGpsManager(context: Context): GpsManager {
        return AndroidGpsManager(LocationServices.getFusedLocationProviderClient(context))
    }
}

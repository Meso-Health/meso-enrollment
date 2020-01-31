package org.watsi.enrollment.managers

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import org.watsi.enrollment.R
import org.watsi.enrollment.device.managers.PreferencesManager
import java.util.Locale
import javax.inject.Inject

class LocaleManager @Inject constructor(private val preferencesManager: PreferencesManager) {
    fun createLocalizedContext(baseContext: Context): Context {
        val config = baseContext.resources.configuration
        preferencesManager.getLocale()?.let { config.setLocale(it) }
        return baseContext.createConfigurationContext(config)
    }

    fun setLocaleConfirmationDialog(activity: Activity) {
        // Add more languages here as we support them.
        // TODO: More this to BuildConfigHelper once that code gets merged.
        val languagesAvailable = listOf(
            Locale.US
        )
        AlertDialog.Builder(activity)
                .setTitle(R.string.menu_switch_language)
                .setSingleChoiceItems(
                    languagesAvailable.map { it.displayLanguage }.toTypedArray(),
                    languagesAvailable.indexOf(getLocale() ?: 0) ,
                    { _, index -> /* no-op */
                        setLocale(languagesAvailable[index], activity)
                    }
                )
                .setNegativeButton(R.string.modal_cancel) { dialogInterface, _ ->
                    (dialogInterface as AlertDialog).dismiss()
                }
                .show()
    }

    private fun setLocale(locale: Locale, activity: Activity) {
        preferencesManager.updateLocale(locale)
        activity.recreate()
    }

    private fun getLocale(): Locale? {
        return preferencesManager.getLocale()
    }
}

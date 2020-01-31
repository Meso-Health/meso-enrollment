package org.watsi.enrollment.activities

import android.content.Context
import dagger.android.support.DaggerAppCompatActivity
import org.watsi.enrollment.BaseApplication
import org.watsi.enrollment.managers.LocaleManager

abstract class LocaleAwareActivity : DaggerAppCompatActivity() {

    lateinit var localeManager: LocaleManager

    override fun attachBaseContext(base: Context) {
        // pull LocaleManager off of Application because Activity is not injected when this is called
        localeManager = (base.applicationContext as BaseApplication).localeManager
        super.attachBaseContext(localeManager.createLocalizedContext(base))
    }
}

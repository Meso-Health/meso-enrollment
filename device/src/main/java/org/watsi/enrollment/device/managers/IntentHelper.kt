package org.watsi.enrollment.device.managers

import android.app.Activity
import android.content.Intent

class IntentHelper {
    fun validIntent(intent: Intent, context: Activity): Boolean {
        return intent.resolveActivity(context.packageManager) != null
    }
}

package org.watsi.enrollment.helpers

import android.content.Context
import org.watsi.enrollment.R
import org.watsi.enrollment.domain.entities.Gender


object TranslationHelper {
    fun getTranslatedMemberDeleteReasonOptions(context: Context): List<Pair<String, String>> {
        return BuildConfigHelper.getPredefinedArchivedReasonList().map { archivedReason ->
            Pair(archivedReason, getStringResourceByName(archivedReason, context))
        }
    }

    fun getTranslatedGenderOptions(context: Context): List<Pair<Gender, String>> {
        return listOf(
            Pair(Gender.F, context.getString(R.string.female)),
            Pair(Gender.M, context.getString(R.string.male))
        )
    }

    fun getTranslatedProfessionOptions(context: Context): List<Pair<String, String>> {
        return BuildConfigHelper.getPredefinedProfessionList().map { professionChoice ->
            Pair(professionChoice, getStringResourceByName(professionChoice, context))
        }
    }

    fun getTranslatedRelationshipToHeadOptions(context: Context): List<Pair<String, String>> {
        return BuildConfigHelper.getPredefinedRelationshipToHeadList().map { relationshipToHead ->
            Pair(relationshipToHead, getStringResourceByName(relationshipToHead, context))
        }
    }

    // Grabbed this method from https://stackoverflow.com/questions/7493287/android-how-do-i-get-string-from-resources-using-its-name
    // If exists, returns the locale specific string, otherwise returns the original string.
    private fun getStringResourceByName(string: String, context: Context): String {
        val packageName = context.packageName
        val resId = context.resources.getIdentifier(string, "string", packageName)
        return if (resId > 0) { context.getString(resId) } else { string }
    }
}

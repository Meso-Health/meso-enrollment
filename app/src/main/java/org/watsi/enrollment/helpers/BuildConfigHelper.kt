package org.watsi.enrollment.helpers

import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.domain.entities.Member

/*
    For different flavours, implementors can define their own lists here.
 */
object BuildConfigHelper {
    val FLAVOR_DEMO = "demo"
    val FLAVOR_UGANDA = "uganda"

    fun getPredefinedProfessionList(): List<String> {
        return when (BuildConfig.FLAVOR) {
            FLAVOR_DEMO -> listOf(
                "farmer",
                "student",
                "driver",
                "unemployed",
                "engineer",
                "disabled",
                "other"
            )
            else -> emptyList()
        }
    }

    fun getPredefinedArchivedReasonList(): List<String> {
        return when (BuildConfig.FLAVOR) {
            FLAVOR_DEMO -> listOf(
                "death",
                "relocation",
                "divorce",
                "no_longer_eligible",
                "unpaid",
                "other"
            )
            FLAVOR_UGANDA -> listOf(
                "death",
                "relocation",
                "no_longer_eligible",
                "other"
            )
            else -> emptyList()
        }
    }

    fun getPredefinedRelationshipToHeadList(): List<String> {
        return when (BuildConfig.FLAVOR) {
            FLAVOR_DEMO -> listOf(
                Member.RELATIONSHIP_TO_HEAD_SELF,
                "spouse",
                "child",
                "parent",
                "grandchild",
                "grandparent",
                "house_staff",
                "other"
            )
            else -> emptyList()
        }
    }
}

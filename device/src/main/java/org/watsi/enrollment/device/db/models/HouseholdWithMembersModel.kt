package org.watsi.enrollment.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.enrollment.domain.relations.HouseholdWithMembers

data class HouseholdWithMembersModel(
    @Embedded var householdModel: HouseholdModel? = null,
    @Relation(parentColumn = "id", entityColumn = "householdId", entity = MemberModel::class)
    var membersWithThumbnails: List<MemberWithThumbnailModel>? = null,
    @Relation(parentColumn = "administrativeDivisionId", entityColumn = "id", entity = AdministrativeDivisionModel::class)
    var administrativeDivisions: List<AdministrativeDivisionModel>? = null) {

    fun toHouseholdWithMembers(): HouseholdWithMembers {
        householdModel?.let {
            val members = membersWithThumbnails?.let {
                it.map { it.toMemberWithThumbnail() }
            } ?: emptyList()
            val administrativeDivision = administrativeDivisions?.
                    firstOrNull()?.
                    toAdministrativeDivision()

            return HouseholdWithMembers(
                    household = it.toHousehold(),
                    members = members,
                    administrativeDivision = administrativeDivision
            )
        }

        throw IllegalStateException("HouseholdModel cannot be null")
    }
}

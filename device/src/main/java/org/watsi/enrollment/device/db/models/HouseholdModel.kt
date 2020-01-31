package org.watsi.enrollment.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.Household
import java.util.UUID

@Entity(tableName = "households",
        indices = [
            Index("administrativeDivisionId")
        ],
        foreignKeys = [
            ForeignKey(
                entity = AdministrativeDivisionModel::class,
                parentColumns = ["id"],
                childColumns = ["administrativeDivisionId"]
            )
        ])
data class HouseholdModel(
    @PrimaryKey val id: UUID,
    val enrolledAt: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
    val administrativeDivisionId: Int,
    val address: String?
){
    fun toHousehold(): Household {
        return Household(
            id = id,
            enrolledAt = enrolledAt,
            administrativeDivisionId = administrativeDivisionId,
            address = address
        )
    }

    companion object {
        fun fromHousehold(household: Household, clock: Clock): HouseholdModel {
            val now = clock.instant()
            return HouseholdModel(
                id = household.id,
                enrolledAt = household.enrolledAt,
                administrativeDivisionId = household.administrativeDivisionId,
                address = household.address,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

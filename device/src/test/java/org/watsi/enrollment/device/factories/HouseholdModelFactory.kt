package org.watsi.enrollment.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.enrollment.device.db.daos.HouseholdDao
import org.watsi.enrollment.device.db.models.HouseholdModel
import java.util.UUID

object HouseholdModelFactory {

    fun build(
            id: UUID = UUID.randomUUID(),
            enrolledAt: Instant? = null,
            createdAt: Instant? = null,
            updatedAt: Instant? = null,
            administrativeDivisionId: Int,
            address: String? = null,
            clock: Clock = Clock.systemUTC()
    ): HouseholdModel {
        val currentTime = Instant.now(clock)
        return HouseholdModel(
                id = id,
                enrolledAt = enrolledAt ?: currentTime,
                createdAt = createdAt ?: currentTime,
                updatedAt = updatedAt ?: currentTime,
                administrativeDivisionId = administrativeDivisionId,
                address = address
        )
    }

    fun create(
            householdDao: HouseholdDao,
            id: UUID = UUID.randomUUID(),
            enrolledAt: Instant? = null,
            createdAt: Instant? = null,
            updatedAt: Instant? = null,
            administrativeDivisionId: Int,
            clock: Clock = Clock.systemUTC()
    ): HouseholdModel {
        val householdModel = build(
                id = id,
                enrolledAt = enrolledAt,
                createdAt = createdAt,
                updatedAt = updatedAt,
                administrativeDivisionId = administrativeDivisionId,
                clock = clock
        )
        householdDao.insert(householdModel, listOf())
        return householdModel
    }
}

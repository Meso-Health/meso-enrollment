package org.watsi.enrollment.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.enrollment.device.db.daos.HouseholdEnrollmentRecordDao
import org.watsi.enrollment.device.db.models.HouseholdEnrollmentRecordModel
import java.util.UUID

object HouseholdEnrollmentRecordModelFactory {

    fun build(
        id: UUID = UUID.randomUUID(),
        householdId: UUID,
        userId: Int = 1,
        enrolledAt: Instant? = null,
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        paying: Boolean = true,
        renewal: Boolean = false,
        administrativeDivisionId: Int,
        enrollmentPeriodId: Int,
        clock: Clock = Clock.systemUTC()
    ): HouseholdEnrollmentRecordModel {
        val currentTime = Instant.now(clock)
        return HouseholdEnrollmentRecordModel(
            id = id,
            householdId = householdId,
            userId = userId,
            enrolledAt = enrolledAt ?: currentTime,
            createdAt = createdAt ?: currentTime,
            updatedAt = updatedAt ?: currentTime,
            paying = paying,
            renewal = renewal,
            administrativeDivisionId = administrativeDivisionId,
            enrollmentPeriodId = enrollmentPeriodId
        )
    }

    fun create(
        householdEnrollmentRecordDao: HouseholdEnrollmentRecordDao,
        id: UUID = UUID.randomUUID(),
        householdId: UUID,
        userId: Int = 1,
        enrolledAt: Instant? = null,
        paying: Boolean = true,
        renewal: Boolean = false,
        administrativeDivisionId: Int,
        enrollmentPeriodId: Int,
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        clock: Clock = Clock.systemUTC()
    ): HouseholdEnrollmentRecordModel {
        val householdEnrollmentRecordModel = build(
            id = id,
            enrolledAt = enrolledAt,
            householdId = householdId,
            userId = userId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            paying = paying,
            renewal = renewal,
            administrativeDivisionId = administrativeDivisionId,
            enrollmentPeriodId = enrollmentPeriodId,
            clock = clock
        )
        householdEnrollmentRecordDao.insert(householdEnrollmentRecordModel)
        return householdEnrollmentRecordModel
    }
}

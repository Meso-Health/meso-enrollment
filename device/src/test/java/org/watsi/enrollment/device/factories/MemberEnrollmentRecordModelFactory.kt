package org.watsi.enrollment.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.enrollment.device.db.daos.MemberEnrollmentRecordDao
import org.watsi.enrollment.device.db.models.MemberEnrollmentRecordModel
import java.util.UUID

object MemberEnrollmentRecordModelFactory {

    fun build(
        id: UUID = UUID.randomUUID(),
        memberId: UUID = UUID.randomUUID(),
        userId: Int = 1,
        enrolledAt: Instant? = null,
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        note: String? = null,
        enrollmentPeriodId: Int,
        clock: Clock = Clock.systemUTC()
    ): MemberEnrollmentRecordModel {
        val currentTime = Instant.now(clock)
        return MemberEnrollmentRecordModel(
            id = id,
            memberId = memberId,
            userId = userId,
            enrolledAt = enrolledAt ?: currentTime,
            createdAt = createdAt ?: currentTime,
            updatedAt = updatedAt ?: currentTime,
            note = note,
            enrollmentPeriodId = enrollmentPeriodId
        )
    }

    fun create(
        memberEnrollmentRecordDao: MemberEnrollmentRecordDao,
        id: UUID = UUID.randomUUID(),
        memberId: UUID = UUID.randomUUID(),
        userId: Int = 1,
        enrolledAt: Instant? = null,
        note: String? = null,
        enrollmentPeriodId: Int,
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        clock: Clock = Clock.systemUTC()
    ): MemberEnrollmentRecordModel {
        val memberEnrollmentRecordModel = build(
            id = id,
            enrolledAt = enrolledAt,
            memberId = memberId,
            userId = userId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            note = note,
            enrollmentPeriodId = enrollmentPeriodId,
            clock = clock
        )
        memberEnrollmentRecordDao.insert(memberEnrollmentRecordModel)
        return memberEnrollmentRecordModel
    }
}

package org.watsi.enrollment.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.enrollment.device.db.daos.MemberDao
import org.watsi.enrollment.device.db.models.MemberModel
import org.watsi.enrollment.domain.entities.DateAccuracy
import org.watsi.enrollment.domain.entities.Gender
import java.util.UUID

object MemberModelFactory {

    fun build(
            id: UUID = UUID.randomUUID(),
            enrolledAt: Instant? = null,
            createdAt: Instant? = null,
            updatedAt: Instant? = null,
            name: String = "random name",
            birthdate: LocalDate = LocalDate.now(),
            birthdateAccuracy: DateAccuracy = DateAccuracy.Y,
            gender: Gender = Gender.F,
            phoneNumber: String? = "123456789",
            photoId: UUID? = UUID.randomUUID(),
            thumbnailPhotoId: UUID? = UUID.randomUUID(),
            photoUrl: String? = null,
            cardId: String? = null,
            membershipNumber: String? = null,
            medicalRecordNumber: String? = null,
            householdId: UUID,
            profession: String? = null,
            relationshipToHead: String? = null,
            archivedAt: Instant? = null,
            archivedReason: String? = null,
            clock: Clock = Clock.systemUTC()
    ): MemberModel {
        val currentTime = Instant.now(clock)
        return MemberModel(
                id = id,
                enrolledAt = enrolledAt ?: currentTime,
                createdAt = createdAt ?: currentTime,
                updatedAt = updatedAt ?: currentTime,
                name = name,
                birthdate = birthdate,
                birthdateAccuracy = birthdateAccuracy,
                gender = gender,
                phoneNumber = phoneNumber,
                photoId = photoId,
                thumbnailPhotoId = thumbnailPhotoId,
                photoUrl = photoUrl,
                cardId = cardId,
                membershipNumber = membershipNumber,
                medicalRecordNumber = medicalRecordNumber,
                householdId = householdId,
                profession = profession,
                relationshipToHead = relationshipToHead,
                archivedAt = archivedAt,
                archivedReason = archivedReason
        )
    }

    fun create(
            memberDao: MemberDao,
            id: UUID = UUID.randomUUID(),
            enrolledAt: Instant? = null,
            createdAt: Instant? = null,
            updatedAt: Instant? = null,
            name: String = "random name",
            birthdate: LocalDate = LocalDate.now(),
            birthdateAccuracy: DateAccuracy = DateAccuracy.Y,
            gender: Gender = Gender.F,
            phoneNumber: String? = "123456789",
            photoId: UUID? = UUID.randomUUID(),
            thumbnailPhotoId: UUID? = UUID.randomUUID(),
            photoUrl: String? = null,
            cardId: String? = null,
            membershipNumber: String? = null,
            medicalRecordNumber: String? = null,
            householdId: UUID,
            profession: String? = null,
            relationshipToHead: String? = null,
            archivedAt: Instant? = null,
            archivedReason: String? = null,
            clock: Clock = Clock.systemUTC()
    ): MemberModel {
        val memberModel = build(
            id = id,
            enrolledAt = enrolledAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            name = name,
            birthdate = birthdate,
            birthdateAccuracy = birthdateAccuracy,
            gender = gender,
            phoneNumber = phoneNumber,
            photoId = photoId,
            thumbnailPhotoId = thumbnailPhotoId,
            photoUrl = photoUrl,
            cardId = cardId,
            membershipNumber = membershipNumber,
            medicalRecordNumber = medicalRecordNumber,
            householdId = householdId,
            profession = profession,
            relationshipToHead = relationshipToHead,
            archivedAt = archivedAt,
            archivedReason = archivedReason,
            clock = clock
        )
        memberDao.insert(memberModel)
        return memberModel
    }
}

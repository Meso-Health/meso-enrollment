package org.watsi.enrollment.domain.factories

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.enrollment.domain.entities.DateAccuracy
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.entities.Member
import java.util.UUID

object MemberFactory {
    fun build(
        id: UUID = UUID.randomUUID(),
        enrolledAt: Instant = Instant.now(),
        name: String = "random name",
        birthdate: LocalDate = LocalDate.now(),
        birthdateAccuracy: DateAccuracy = DateAccuracy.Y,
        gender: Gender = Gender.F,
        phoneNumber: String? = "123456789",
        photoId: UUID? = UUID.randomUUID(),
        thumbnailPhotoId: UUID? = null,
        photoUrl: String? = null,
        cardId: String = "RWI123456",
        membershipNumber: String? = "123456789",
        medicalRecordNumber: String? = null,
        householdId: UUID = UUID.randomUUID(),
        profession: String? = null,
        relationshipToHead: String? = null,
        archivedAt: Instant? = null,
        archivedReason: String? = null
    ) : Member {
        return Member(id, enrolledAt, name, birthdate, birthdateAccuracy, gender, phoneNumber,
                photoId, thumbnailPhotoId, photoUrl, cardId, membershipNumber, medicalRecordNumber,
                householdId, profession, relationshipToHead, archivedAt, archivedReason)
    }

    fun headOfHousehold() = build(relationshipToHead = Member.RELATIONSHIP_TO_HEAD_SELF)
    fun qualifyingBeneficiary() = build(
        relationshipToHead = "other",
        profession = "farmer",
        birthdate = LocalDate.now().minusYears(18)
    )
    fun unpaidQualifyingBeneficiary() = build(
        relationshipToHead = "other",
        profession = "farmer",
        birthdate = LocalDate.now().minusYears(18),
        archivedAt = Instant.now(),
        archivedReason = Member.ARCHIVED_REASON_UNPAID
    )
}

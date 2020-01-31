package org.watsi.enrollment.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.enrollment.domain.entities.DateAccuracy
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.entities.Member
import java.util.UUID

@Entity(tableName = "members",
        indices = [
            Index("householdId"),
            Index("cardId", unique = true),
            Index("membershipNumber"),
            Index("photoId")
        ],
        foreignKeys = [
            ForeignKey(
                entity = HouseholdModel::class,
                parentColumns = ["id"],
                childColumns = ["householdId"]
            )
        ]
)
data class MemberModel(@PrimaryKey val id: UUID,
                       val enrolledAt: Instant,
                       val createdAt: Instant,
                       val updatedAt: Instant,
                       val name: String,
                       val birthdate: LocalDate,
                       val birthdateAccuracy: DateAccuracy,
                       val gender: Gender,
                       val phoneNumber: String?,
                       val photoId: UUID?,
                       val thumbnailPhotoId: UUID?,
                       val photoUrl: String?,
                       val cardId: String?,
                       val membershipNumber: String?,
                       val medicalRecordNumber: String? = null,
                       val householdId: UUID,
                       val profession: String?,
                       val relationshipToHead: String?,
                       val archivedAt: Instant?,
                       val archivedReason: String?) {

    init {
        if (name.isEmpty()) {
            throw ModelValidationException("Name cannot be blank")
        }
        if (phoneNumber?.isEmpty() == true) {
            throw ModelValidationException("Phone number cannot be blank")
        }
        if (medicalRecordNumber?.isEmpty() == true) {
            throw ModelValidationException("Medical record number cannot be blank")
        }
    }

    fun toMember(): Member {
        return Member(id = id,
                      enrolledAt = enrolledAt,
                      name = name,
                      birthdate = birthdate,
                      birthdateAccuracy = birthdateAccuracy,
                      gender = gender,
                      phoneNumber = phoneNumber,
                      photoId = photoId,
                      photoUrl = photoUrl,
                      thumbnailPhotoId = thumbnailPhotoId,
                      cardId = cardId,
                      membershipNumber = membershipNumber,
                      medicalRecordNumber = medicalRecordNumber,
                      householdId = householdId,
                      profession = profession,
                      relationshipToHead = relationshipToHead,
                      archivedAt = archivedAt,
                      archivedReason = archivedReason)
    }

    companion object {
        fun fromMember(member: Member, clock: Clock): MemberModel {
            val now = clock.instant()
            return MemberModel(id = member.id,
                               enrolledAt = member.enrolledAt,
                               name = member.name,
                               birthdate = member.birthdate,
                               birthdateAccuracy = member.birthdateAccuracy,
                               gender = member.gender,
                               phoneNumber = member.phoneNumber,
                               photoId = member.photoId,
                               thumbnailPhotoId = member.thumbnailPhotoId,
                               photoUrl = member.photoUrl,
                               cardId = member.cardId,
                               membershipNumber = member.membershipNumber,
                               medicalRecordNumber = member.medicalRecordNumber,
                               householdId = member.householdId,
                               createdAt = now,
                               updatedAt = now,
                               profession = member.profession,
                               relationshipToHead = member.relationshipToHead,
                               archivedAt = member.archivedAt,
                               archivedReason = member.archivedReason)
        }
    }
}

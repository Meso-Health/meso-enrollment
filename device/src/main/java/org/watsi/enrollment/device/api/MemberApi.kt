package org.watsi.enrollment.device.api

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.enrollment.domain.entities.DateAccuracy
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.entities.Member
import java.util.UUID

data class MemberApi(
    val id: UUID,
    val enrolledAt: Instant,
    val fullName: String,
    val birthdate: LocalDate,
    val birthdateAccuracy: DateAccuracy = DateAccuracy.Y,
    val gender: Gender,
    val phoneNumber: String?,
    @Expose(serialize = false) val photoUrl: String? = null, // We only need this when fetching.
    val cardId: String? = null,
    val membershipNumber: String?,
    val medicalRecordNumber: String?,
    val householdId: UUID,
    val profession: String?,
    val relationshipToHead: String?,
    val archivedAt: Instant?,
    val archivedReason: String?
) {
    constructor(member: Member): this(
        id = member.id,
        enrolledAt = member.enrolledAt,
        fullName = member.name,
        birthdate = member.birthdate,
        birthdateAccuracy = member.birthdateAccuracy,
        gender = member.gender,
        phoneNumber = member.phoneNumber,
        cardId = member.cardId,
        membershipNumber = member.membershipNumber,
        medicalRecordNumber = member.medicalRecordNumber,
        householdId = member.householdId,
        profession = member.profession,
        relationshipToHead = member.relationshipToHead,
        archivedAt = member.archivedAt,
        archivedReason = member.archivedReason
    )

    fun toMember(persistedMember: Member?): Member {
        // do not overwrite the local thumbnail photo if the fetched photo is not different
        val thumbnailPhotoId = persistedMember?.let {
            if (it.photoUrl == photoUrl || it.photoUrl == null) it.thumbnailPhotoId else null
        }

        return Member(
            id = id,
            enrolledAt = enrolledAt,
            name = fullName,
            birthdate = birthdate,
            birthdateAccuracy = birthdateAccuracy,
            gender = gender,
            phoneNumber = phoneNumber,
            photoId = persistedMember?.photoId,
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

    companion object {
        const val ID_FIELD = "id"
        const val NAME_FIELD = "full_name"
        const val GENDER_FIELD = "gender"
        const val BIRTHDATE_FIELD = "birthdate"
        const val BIRTHDATE_ACCURACY_FIELD = "birthdate_accuracy"
        const val PHONE_NUMBER_FIELD = "phone_number"
        const val MEDICAL_RECORD_NUMBER_FIELD = "medical_record_number"
        const val MEMBERSHIP_NUMBER_FIELD = "membership_number"
        const val CARD_ID_FIELD = "card_id"
        const val PROFESSION_FIELD = "profession"
        const val RELATIONSHIP_TO_HEAD_FIELD = "relationship_to_head"
        const val ARCHIVED_AT_FIELD = "archived_at"
        const val ARCHIVED_REASON = "archived_reason"

        fun patch(member: Member, deltas: List<Delta>): JsonObject {
            val memberParams = JsonObject()
            memberParams.addProperty(ID_FIELD, member.id.toString())
            deltas.forEach { delta ->
                when (delta.field) {
                    "name" -> memberParams.addProperty(NAME_FIELD, member.name)
                    "gender" -> memberParams.addProperty(GENDER_FIELD, member.gender.toString())
                    "birthdate" -> memberParams.addProperty(BIRTHDATE_FIELD, member.birthdate.toString())
                    "birthdateAccuracy" -> {
                        memberParams.addProperty(BIRTHDATE_ACCURACY_FIELD, member.birthdateAccuracy.toString())
                    }
                    "phoneNumber" -> memberParams.addProperty(PHONE_NUMBER_FIELD, member.phoneNumber)
                    "medicalRecordNumber" -> memberParams.addProperty(MEDICAL_RECORD_NUMBER_FIELD, member.medicalRecordNumber)
                    "membershipNumber" -> memberParams.addProperty(MEMBERSHIP_NUMBER_FIELD, member.membershipNumber)
                    "cardId" -> memberParams.addProperty(CARD_ID_FIELD, member.cardId)
                    "profession" -> memberParams.addProperty(PROFESSION_FIELD, member.profession)
                    "relationshipToHead" -> memberParams.addProperty(RELATIONSHIP_TO_HEAD_FIELD, member.relationshipToHead.toString())
                    "archivedAt" -> memberParams.addProperty(ARCHIVED_AT_FIELD, member.archivedAt.toString())
                    "archivedReason" -> memberParams.addProperty(ARCHIVED_REASON, member.archivedReason)
                    null -> Unit
                }
            }
            return memberParams
        }
    }
}

package org.watsi.enrollment.domain.entities

import com.google.gson.GsonBuilder
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.enrollment.domain.relations.HouseholdWithMembersAndPayments
import org.watsi.enrollment.domain.utils.DateUtils
import java.io.Serializable
import java.util.UUID

data class Member(val id: UUID,
                  val enrolledAt: Instant,
                  val name: String,
                  val birthdate: LocalDate,
                  val birthdateAccuracy: DateAccuracy = DateAccuracy.Y,
                  val gender: Gender,
                  val phoneNumber: String?,
                  val photoId: UUID?,
                  val thumbnailPhotoId: UUID?,
                  val photoUrl: String? = null,
                  val cardId: String? = null,
                  val membershipNumber: String? = null,
                  val medicalRecordNumber: String? = null,
                  val householdId: UUID,
                  val profession: String? = null,
                  val relationshipToHead: String? = null,
                  val archivedAt: Instant?,
                  val archivedReason: String?
): Serializable {

    fun getAgeMonths(clock: Clock = Clock.systemDefaultZone()): Int {
        return DateUtils.getMonthsAgo(birthdate, clock)
    }

    fun getAgeYears(clock: Clock = Clock.systemDefaultZone()): Int {
        return DateUtils.getYearsAgo(birthdate, clock)
    }

    fun getAgeDays(clock: Clock): Int {
        return DateUtils.getDaysAgo(birthdate, clock)
    }

    fun photoExists(): Boolean {
        return photoUrl != null
    }

    fun copyAndUpdatePaymentStatus(
        household: HouseholdWithMembersAndPayments,
        enrollmentPeriod: EnrollmentPeriod,
        clock: Clock
    ): Member {
        val unpaid = household.memberRequiresFee(this, enrollmentPeriod)

        return if (archivedReason == null && unpaid) {
            this.copy(archivedAt = clock.instant(), archivedReason = ARCHIVED_REASON_UNPAID)
        } else if (archivedReason == ARCHIVED_REASON_UNPAID && !unpaid) {
            this.copyAndRestore()
        } else {
            // If the payment status is correct, return the unchanged member
            this
        }
    }

    fun archivedPermanently(): Boolean {
        return this.archivedReason != null && !this.unpaid()
    }

    fun unpaid(): Boolean {
        return this.archivedReason == ARCHIVED_REASON_UNPAID
    }

    fun copyAndRestore(): Member {
        return this.copy(archivedReason = null, archivedAt = null)
    }

    fun diff(previous: Member): List<Delta> {
        val gson = GsonBuilder().serializeNulls().create()
        val previousMap = gson.fromJson(gson.toJson(previous), Map::class.java) as Map<String, Any?>
        val currentMap = gson.fromJson(gson.toJson(this), Map::class.java) as Map<String, Any?>
        val diffFields = currentMap.keys.filter { currentMap[it] != previousMap[it] }

        return diffFields.map {
            Delta(action = Delta.Action.EDIT,
                  modelName = Delta.ModelName.MEMBER,
                  modelId = id,
                  field = it)
        }
    }

    fun isHeadOfHousehold(): Boolean {
        return relationshipToHead == RELATIONSHIP_TO_HEAD_SELF
    }

    companion object {
        const val MAX_AGE = 200
        const val ARCHIVED_REASON_UNPAID = "unpaid"
        val RELATIONSHIP_TO_HEAD_SELF = "SELF"

        fun isValidFullName(name: String, minLength: Int): Boolean {
            return name.split(' ').filter{ it.isNotBlank() }.count() >= minLength
        }

        fun isValidMedicalRecordNumber(medicalRecordNumber: String, minLength: Int, maxLength: Int): Boolean {
            return medicalRecordNumber.length in minLength..maxLength
        }

        fun isValidCardId(cardId: String): Boolean {
            return cardId.matches(Regex("[A-Z]{3}[0-9]{6}"))
        }
    }
}

enum class Gender { M, F }
enum class DateAccuracy { Y, M, D }

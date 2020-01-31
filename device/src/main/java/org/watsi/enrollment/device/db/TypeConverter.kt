package org.watsi.enrollment.device.db

import android.arch.persistence.room.TypeConverter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.watsi.enrollment.domain.entities.DateAccuracy
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.Gender
import java.util.UUID

class TypeConverter {
    private val formatter = DateTimeFormatter.ISO_DATE
    private val LIST_DELIMITER = ","

    @TypeConverter
    fun fromUuid(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUuid(uuidString: String?): UUID? = if (uuidString == null) null else UUID.fromString(uuidString)

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(long: Long?): Instant? = if (long == null) null else Instant.ofEpochMilli(long)

    @TypeConverter
    fun fromLocalDate(localDate: LocalDate?): String? = localDate?.format(formatter)

    @TypeConverter
    fun toLocalDate(string: String?): LocalDate? = if (string == null) null else LocalDate.parse(string, formatter)

    @TypeConverter
    fun fromGender(gender: Gender?): String? {
        return when (gender) {
            Gender.M -> "M"
            Gender.F -> "F"
            else -> null
        }
    }

    @TypeConverter
    fun toGender(value: String?): Gender? {
        return when (value) {
            "M" -> Gender.M
            "F" -> Gender.F
            else -> null
        }
    }

    @TypeConverter
    fun fromDateAccuracy(accuracy: DateAccuracy?): String? {
        return when (accuracy) {
            DateAccuracy.Y -> "Y"
            DateAccuracy.M -> "M"
            DateAccuracy.D -> "D"
            else -> null
        }
    }

    @TypeConverter
    fun toDateAccuracy(value: String?): DateAccuracy? {
        return when (value) {
            "Y" -> DateAccuracy.Y
            "M" -> DateAccuracy.M
            "D" -> DateAccuracy.D
            else -> null
        }
    }

    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return if (list != null) {
            return list.joinToString(LIST_DELIMITER)
        } else {
            null
        }
    }

    @TypeConverter
    fun toList(string: String?): List<String>? {
        return if (string != null) {
            string.split(LIST_DELIMITER)
        } else {
            null
        }
    }

    @TypeConverter
    fun toAction(value: String?): Delta.Action? {
        return if (value != null) {
            Delta.Action.valueOf(value)
        } else {
            null
        }
    }

    @TypeConverter
    fun fromAction(action: Delta.Action?): String? {
        return if (action != null) {
            return action.toString()
        } else {
            null
        }
    }

    @TypeConverter
    fun toModelName(value: String?): Delta.ModelName? {
        return if (value != null) {
            Delta.ModelName.valueOf(value)
        } else {
            null
        }
    }

    @TypeConverter
    fun fromModelName(modelName: Delta.ModelName?): String? {
        return if (modelName != null) {
            modelName.toString()
        } else {
            null
        }
    }
}

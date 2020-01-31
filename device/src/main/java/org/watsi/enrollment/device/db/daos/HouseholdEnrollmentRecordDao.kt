package org.watsi.enrollment.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Single
import org.watsi.enrollment.device.db.models.DeltaModel
import org.watsi.enrollment.device.db.models.HouseholdEnrollmentRecordModel
import java.util.UUID

@Dao
interface HouseholdEnrollmentRecordDao {
    @Insert
    fun insert(householdEnrollmentRecordModel: HouseholdEnrollmentRecordModel)

    @Insert
    fun insert(householdEnrollmentRecordModel: HouseholdEnrollmentRecordModel, deltaModel: DeltaModel)

    @Query("SELECT * FROM householdEnrollmentRecords WHERE id = :id LIMIT 1")
    fun get(id: UUID): Single<HouseholdEnrollmentRecordModel>

    @Query("DELETE from householdEnrollmentRecords")
    fun deleteAll()
}

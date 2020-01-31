package org.watsi.enrollment.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Single
import org.watsi.enrollment.device.db.models.DeltaModel
import org.watsi.enrollment.device.db.models.MemberEnrollmentRecordModel
import java.util.UUID

@Dao
interface MemberEnrollmentRecordDao {
    @Insert
    fun insert(memberEnrollmentRecordModel: MemberEnrollmentRecordModel)

    @Insert
    fun insert(memberEnrollmentRecordModel: MemberEnrollmentRecordModel, deltaModel: DeltaModel)

    @Query("SELECT * FROM memberEnrollmentRecords WHERE id = :id LIMIT 1")
    fun get(id: UUID): Single<MemberEnrollmentRecordModel>

    @Query("DELETE FROM memberEnrollmentRecords")
    fun deleteAll()
}

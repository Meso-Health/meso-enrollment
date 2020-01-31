package org.watsi.enrollment.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import io.reactivex.Single
import org.watsi.enrollment.device.db.models.MemberWithRawPhotoModel
import org.watsi.enrollment.device.db.models.PhotoModel
import java.util.UUID

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE id = :id LIMIT 1")
    fun get(id: UUID): Single<PhotoModel>

    @Transaction
    @Query("SELECT * FROM members WHERE id = :memberId LIMIT 1")
    fun getMemberWithRawPhoto(memberId: UUID): Single<MemberWithRawPhotoModel>

    @Insert
    fun insert(photo: PhotoModel)
}

package org.watsi.enrollment.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import org.watsi.enrollment.device.db.models.AdministrativeDivisionModel

@Dao
interface AdministrativeDivisionDao {
    @Query("SELECT * FROM administrativeDivisions WHERE level = :level")
    fun allWithLevel(level: String): Flowable<List<AdministrativeDivisionModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(models: List<AdministrativeDivisionModel>)

    @Query("DELETE from administrativeDivisions")
    fun deleteAll()
}

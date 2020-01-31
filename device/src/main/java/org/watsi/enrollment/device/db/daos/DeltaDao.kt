package org.watsi.enrollment.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import io.reactivex.Flowable
import io.reactivex.Single
import org.watsi.enrollment.device.db.models.DeltaModel
import org.watsi.enrollment.domain.entities.Delta
import java.util.UUID

@Dao
interface DeltaDao {
    @Query("SELECT * FROM deltas")
    fun getAll(): Single<List<DeltaModel>>

    @Insert
    fun insert(deltaModel: DeltaModel)

    @Update
    fun update(deltaModels: List<DeltaModel>)

    @Query("SELECT * FROM deltas WHERE synced = 0 AND modelName = :modelName")
    fun unsynced(modelName: Delta.ModelName): Single<List<DeltaModel>>

    @Query("SELECT DISTINCT(modelId) FROM deltas WHERE synced = 0 AND modelName = :modelName AND action = :action")
    fun unsyncedModelIds(modelName: Delta.ModelName, action: Delta.Action): Single<List<UUID>>

    @Query("SELECT COUNT(DISTINCT(modelId)) FROM deltas WHERE synced = 0 AND modelName = :modelName")
    fun countUnsynced(modelName: Delta.ModelName): Flowable<Int>

    @Query("DELETE from deltas")
    fun deleteAll()
}

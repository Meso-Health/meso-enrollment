package org.watsi.enrollment.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import android.arch.persistence.room.Update
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.watsi.enrollment.device.db.models.DeltaModel
import org.watsi.enrollment.device.db.models.MemberModel
import org.watsi.enrollment.device.db.models.MemberWithThumbnailModel
import java.util.UUID

@Dao
abstract class MemberDao {

    @Query("SELECT * FROM members")
    abstract fun all(): Flowable<List<MemberModel>>

    @Transaction
    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    abstract fun getFlowable(id: UUID): Flowable<MemberWithThumbnailModel>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    abstract fun get(id: UUID): Single<MemberModel>

    @Insert
    abstract fun insert(member: MemberModel)

    @Update
    abstract fun update(member: MemberModel)

    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    abstract fun insertDeltas(deltas: List<DeltaModel>)

    @Transaction
    open fun updateWithDeltas(member: MemberModel, deltas: List<DeltaModel>) {
        update(member)
        insertDeltas(deltas)
    }

    @Query("SELECT * FROM members where cardId = :id LIMIT 1")
    abstract fun withCardId(id: String): Maybe<MemberModel>

    @Query("SELECT * FROM members where membershipNumber = :id LIMIT 1")
    abstract fun withMembershipNumber(id: String): Maybe<MemberModel>

    @Insert
    abstract fun insertWithDeltas(memberModel: MemberModel, deltas: List<DeltaModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun upsert(model: MemberModel)

    @Transaction
    @Query("SELECT * FROM members WHERE members.id IN (:ids)")
    abstract fun findMemberRelationsByIds(ids: List<UUID>): Single<List<MemberWithThumbnailModel>>

    @Transaction
    @Query("SELECT * FROM members WHERE members.name IN (:names)")
    abstract fun findMemberRelationsByName(names: List<String>): Single<List<MemberWithThumbnailModel>>

    @Transaction
    @Query("SELECT * FROM members WHERE members.cardId IN (:cardIds)")
    abstract fun findMemberRelationsByCardId(cardIds: List<String>): Single<List<MemberWithThumbnailModel>>

    @Query("SELECT * FROM members WHERE photoUrl IS NOT NULL AND thumbnailPhotoId IS NULL")
    abstract fun needPhotoDownload(): Single<List<MemberModel>>

    @Query("SELECT count(*) FROM members WHERE photoUrl IS NOT NULL AND thumbnailPhotoId IS NULL")
    abstract fun needPhotoDownloadCount(): Flowable<Int>

    @Query("DELETE from members")
    abstract fun deleteAll()

    @Query("UPDATE members SET membershipNumber = :membershipNumber WHERE id = :id")
    abstract fun updateMembershipNumber(id: UUID, membershipNumber: String)

    @Query("SELECT DISTINCT(cardId) FROM members WHERE cardId IS NOT NULL")
    abstract fun allDistinctCardIds(): Single<List<String>>

    @Query("SELECT DISTINCT(name) FROM members")
    abstract fun allDistinctNames(): Single<List<String>>
}

package org.watsi.enrollment.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import io.reactivex.Flowable
import io.reactivex.Single
import org.threeten.bp.Instant
import org.watsi.enrollment.device.db.models.DeltaModel
import org.watsi.enrollment.device.db.models.HouseholdEnrollmentRecordModel
import org.watsi.enrollment.device.db.models.HouseholdModel
import org.watsi.enrollment.device.db.models.HouseholdWithMembersAndPaymentsModel
import org.watsi.enrollment.device.db.models.HouseholdWithMembersModel
import org.watsi.enrollment.device.db.models.MemberEnrollmentRecordModel
import org.watsi.enrollment.device.db.models.MemberModel
import org.watsi.enrollment.device.db.models.MembershipPaymentModel
import org.watsi.enrollment.domain.entities.Delta
import java.util.UUID

@Dao
interface HouseholdDao {
    @Insert
    fun insert(householdModel: HouseholdModel, memberModels: List<MemberModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(
        householdModels: List<HouseholdModel>,
        memberModels: List<MemberModel>,
        householdEnrollmentRecordModels: List<HouseholdEnrollmentRecordModel>,
        memberEnrollmentRecordModels: List<MemberEnrollmentRecordModel>,
        paymentModels: List<MembershipPaymentModel>
    )

    @Transaction
    @Query("""
        SELECT households.*, MAX(households.enrolledAt, MAX(COALESCE(membersJoin.enrolledAt, 0)), MAX(COALESCE(memberDeltasJoin.createdAt, 0)), MAX(COALESCE(householdEnrollmentRecordsJoin.enrolledAt, 0)), MAX(COALESCE(paymentDeltasJoin.createdAt, 0))) as editedAt
        FROM households
        LEFT OUTER JOIN members as membersJoin ON households.id = membersJoin.householdId
        LEFT OUTER JOIN deltas as memberDeltasJoin ON memberDeltasJoin.modelId = membersJoin.id
        LEFT OUTER JOIN householdEnrollmentRecords as householdEnrollmentRecordsJoin ON householdEnrollmentRecordsJoin.householdId = households.id
		LEFT OUTER JOIN membershipPayments as membershipPaymentsJoin ON membershipPaymentsJoin.householdEnrollmentRecordId = householdEnrollmentRecordsJoin.id
        LEFT OUTER JOIN deltas as paymentDeltasJoin ON paymentDeltasJoin.modelId = membershipPaymentsJoin.id
        GROUP BY households.id
        HAVING editedAt >= :start
        ORDER BY editedAt DESC
        """)
    fun createdOrEditedAfter(start: Instant): Flowable<List<HouseholdWithMembersModel>>

    @Transaction
    @Query("SELECT * FROM households where id = :id LIMIT 1")
    fun getFlowable(id: UUID): Flowable<HouseholdWithMembersAndPaymentsModel>

    @Transaction
    @Query("SELECT * FROM households where id = :id LIMIT 1")
    fun get(id: UUID): Single<HouseholdModel>

    @Insert
    fun insertWithDelta(
        householdModel: HouseholdModel,
        deltaModel: DeltaModel
    )

    @Query("DELETE from households")
    fun deleteAll()
}

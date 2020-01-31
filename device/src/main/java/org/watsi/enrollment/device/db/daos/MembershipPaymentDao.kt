package org.watsi.enrollment.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Single
import org.watsi.enrollment.device.db.models.DeltaModel
import org.watsi.enrollment.device.db.models.MembershipPaymentModel
import java.util.UUID

@Dao
interface MembershipPaymentDao {
    @Insert
    fun insertWithDelta(membershipPaymentModel: MembershipPaymentModel, deltas: List<DeltaModel>)

    @Query("SELECT * FROM membershipPayments WHERE id = :id LIMIT 1")
    fun get(id: UUID): Single<MembershipPaymentModel>

    @Query("""
        SELECT membershipPayments.* FROM membershipPayments
        JOIN householdEnrollmentRecords ON householdEnrollmentRecords.id = membershipPayments.householdEnrollmentRecordId
        JOIN households ON householdEnrollmentRecords.householdId = households.id
        WHERE households.id = :householdId
        """)
    fun findByHousehold(householdId: UUID): Flowable<List<MembershipPaymentModel>>

    @Query("""
        SELECT COALESCE(SUM(
            annualContributionFee +
            qualifyingBeneficiariesFee +
            registrationFee +
            penaltyFee +
            cardReplacementFee +
            otherFee), 0)
        FROM membershipPayments""")
    fun totalCollected(): Flowable<Int>

    @Query("DELETE from membershipPayments")
    fun deleteAll()
}

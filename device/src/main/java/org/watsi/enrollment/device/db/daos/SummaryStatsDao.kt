package org.watsi.enrollment.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import io.reactivex.Single
import org.watsi.enrollment.domain.entities.PaymentFees

@Dao
abstract class SummaryStatsDao {

    @Query("""
        SELECT COUNT(*)
        FROM members
        INNER JOIN households ON households.id = members.householdId
        INNER JOIN householdEnrollmentRecords ON
            householdEnrollmentRecords.householdId = households.id
        WHERE (members.relationshipToHead = 'SELF') = :isMember AND
            archivedAt IS NULL AND
            households.administrativeDivisionId IN (:adminDivisionIds) AND
            householdEnrollmentRecords.paying IN (:payingFilter) AND
            members.gender IN (:genderFilter) AND
            householdEnrollmentRecords.enrollmentPeriodId = :enrollmentPeriodId AND
            householdEnrollmentRecords.renewal IN (:renewalFilter)
        """)
    abstract fun countEnrolled(
        isMember: Boolean,
        adminDivisionIds: List<Int>,
        payingFilter: List<Boolean>,
        genderFilter: List<String>,
        renewalFilter: List<Boolean>,
        enrollmentPeriodId: Int
    ): Single<Int>

    @Query("""
        SELECT
            SUM(COALESCE(registrationFee, 0)) AS initialEnrollmentFee,
            SUM(COALESCE(annualContributionFee, 0)) AS annualContributionFee,
            SUM(COALESCE(cardReplacementFee, 0)) AS cardFee,
            0 AS beneficiariesCount,
            SUM(COALESCE(qualifyingBeneficiariesFee, 0)) AS beneficiariesFee,
            SUM(COALESCE(penaltyFee, 0)) AS penaltyFee,
            SUM(COALESCE(otherFee, 0)) AS otherFee
        FROM membershipPayments
        JOIN householdEnrollmentRecords ON householdEnrollmentRecords.id = membershipPayments.householdEnrollmentRecordId
        JOIN households ON householdEnrollmentRecords.householdId = households.id
        JOIN members ON members.householdId = households.id
        WHERE households.administrativeDivisionId IN (:adminDivisionIds) AND
            householdEnrollmentRecords.paying IN (:payingFilter) AND
            (members.gender IN (:genderFilter) AND members.relationshipToHead = "SELF") AND
            householdEnrollmentRecords.enrollmentPeriodId = :enrollmentPeriodId AND
            householdEnrollmentRecords.renewal IN (:renewalFilter)
        """)
    abstract fun totalCollected(
        adminDivisionIds: List<Int>,
        payingFilter: List<Boolean>,
        genderFilter: List<String>,
        renewalFilter: List<Boolean>,
        enrollmentPeriodId: Int
    ): Single<PaymentFees>
}

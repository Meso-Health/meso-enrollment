package org.watsi.enrollment.device.db.daos

import org.junit.Before
import org.junit.Test
import org.watsi.enrollment.device.factories.AdministrativeDivisionModelFactory
import org.watsi.enrollment.device.factories.EnrollmentPeriodModelFactory
import org.watsi.enrollment.device.factories.HouseholdEnrollmentRecordModelFactory
import org.watsi.enrollment.device.factories.HouseholdModelFactory
import org.watsi.enrollment.device.factories.MemberModelFactory
import org.watsi.enrollment.device.factories.MembershipPaymentModelFactory
import org.watsi.enrollment.domain.entities.AdministrativeDivision
import org.watsi.enrollment.domain.entities.EnrollmentPeriod
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.entities.Household
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.PaymentFees

class SummaryStatsDaoTest : DaoBaseTest() {

    lateinit var administrativeDivision: AdministrativeDivision
    lateinit var enrollmentPeriod: EnrollmentPeriod

    @Before
    fun setup() {
        administrativeDivision = AdministrativeDivisionModelFactory.create(administrativeDivisionDao)
                .toAdministrativeDivision()
        enrollmentPeriod = EnrollmentPeriodModelFactory.create(enrollmentPeriodDao).toEnrollmentPeriod()
    }

    private fun createEnrolledHousehold(): Household {
        val enrolledHousehold = HouseholdModelFactory.create(
            householdDao,
            administrativeDivisionId = administrativeDivision.id
        )
        HouseholdEnrollmentRecordModelFactory.create(
            householdEnrollmentRecordDao,
            householdId = enrolledHousehold.id,
            administrativeDivisionId = administrativeDivision.id,
            enrollmentPeriodId = enrollmentPeriod.id
        )
        MemberModelFactory.create(memberDao,
            householdId = enrolledHousehold.id, relationshipToHead = Member.RELATIONSHIP_TO_HEAD_SELF)
        MemberModelFactory.create(memberDao,
            householdId = enrolledHousehold.id, relationshipToHead = "spouse")
        MemberModelFactory.create(memberDao,
            householdId = enrolledHousehold.id, relationshipToHead = "child")
        return enrolledHousehold.toHousehold()
    }

    private fun createUnenrolledHousehold() {
        val unenrolledHousehold = HouseholdModelFactory.create(
            householdDao,
            administrativeDivisionId = administrativeDivision.id
        )
        MemberModelFactory.create(memberDao,
            householdId = unenrolledHousehold.id, relationshipToHead = Member.RELATIONSHIP_TO_HEAD_SELF)
        MemberModelFactory.create(memberDao,
            householdId = unenrolledHousehold.id, relationshipToHead = "spouse")
        MemberModelFactory.create(memberDao,
            householdId = unenrolledHousehold.id, relationshipToHead = "child")
    }

    @Test
    fun countEnrolled_isMember() {
        createEnrolledHousehold()
        createEnrolledHousehold()
        createUnenrolledHousehold()

        summaryStatsDao.countEnrolled(
            isMember = true,
            adminDivisionIds = listOf(administrativeDivision.id),
            payingFilter = listOf(true),
            genderFilter = listOf("F"),
            renewalFilter = listOf(false),
            enrollmentPeriodId = enrollmentPeriod.id
        ).test().assertValue(2)
    }

    @Test
    fun countEnrolled_isNotMember() {
        createEnrolledHousehold()
        createEnrolledHousehold()
        createUnenrolledHousehold()

        summaryStatsDao.countEnrolled(
            isMember = false,
            adminDivisionIds = listOf(administrativeDivision.id),
            payingFilter = listOf(true),
            genderFilter = listOf("F"),
            renewalFilter = listOf(false),
            enrollmentPeriodId = enrollmentPeriod.id
        ).test().assertValue(4)
    }

    @Test
    fun totalCollected_noRecords() {
        val result = PaymentFees(0, 0, 0, 0, 0, 0, 0)
        summaryStatsDao.totalCollected(
            adminDivisionIds = emptyList(),
            payingFilter = emptyList(),
            genderFilter = emptyList(),
            renewalFilter = listOf(false),
            enrollmentPeriodId = enrollmentPeriod.id
        ).test().assertValue(result)
    }

    @Test
    fun totalCollected_someRecords() {
        val household = HouseholdModelFactory.create(
            householdDao,
            administrativeDivisionId = administrativeDivision.id
        )
        MemberModelFactory.create(
            memberDao = memberDao,
            gender = Gender.F,
            relationshipToHead = Member.RELATIONSHIP_TO_HEAD_SELF,
            householdId = household.id
        )
        val enrollmentRecord = HouseholdEnrollmentRecordModelFactory.create(
            householdEnrollmentRecordDao = householdEnrollmentRecordDao,
            householdId = household.id,
            administrativeDivisionId = administrativeDivision.id,
            enrollmentPeriodId = enrollmentPeriod.id
        )
        MembershipPaymentModelFactory.create(membershipPaymentDao,
            annualContributionFee = 150,
            qualifyingBeneficiariesFee = 50,
            registrationFee = 0,
            penaltyFee = 0,
            cardReplacementFee = 0,
            otherFee = 0,
            householdEnrollmentRecordId = enrollmentRecord.id
        )
        MembershipPaymentModelFactory.create(membershipPaymentDao,
            annualContributionFee = 240,
            qualifyingBeneficiariesFee = 120,
            registrationFee = 10,
            penaltyFee = 17,
            cardReplacementFee = 19,
            otherFee = 6,
            householdEnrollmentRecordId = enrollmentRecord.id
        )

        val adminDivisionIds = listOf(household.administrativeDivisionId)
        val result = PaymentFees(10, 390, 19, 0, 170, 17, 6)
        summaryStatsDao.totalCollected(
            adminDivisionIds = adminDivisionIds,
            payingFilter = listOf(true),
            genderFilter = listOf("F"),
            renewalFilter = listOf(false),
            enrollmentPeriodId = enrollmentPeriod.id
        ).test().assertValue(result)
    }
}

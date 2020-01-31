package org.watsi.enrollment.device.db.daos

import org.junit.Test
import org.watsi.enrollment.device.factories.AdministrativeDivisionModelFactory
import org.watsi.enrollment.device.factories.EnrollmentPeriodModelFactory
import org.watsi.enrollment.device.factories.HouseholdEnrollmentRecordModelFactory
import org.watsi.enrollment.device.factories.HouseholdModelFactory
import org.watsi.enrollment.device.factories.MembershipPaymentModelFactory

class MembershipPaymentDaoTest : DaoBaseTest() {

    @Test
    fun findByHousehold() {
        val administrativeDivision = AdministrativeDivisionModelFactory.create(administrativeDivisionDao)
        val enrollmentPeriod = EnrollmentPeriodModelFactory.create(enrollmentPeriodDao)
        val householdModel = HouseholdModelFactory.create(
            householdDao = householdDao,
            administrativeDivisionId = administrativeDivision.id
        )
        val householdEnrollmentRecordModel = HouseholdEnrollmentRecordModelFactory.create(
            householdEnrollmentRecordDao = householdEnrollmentRecordDao,
            householdId = householdModel.id,
            administrativeDivisionId = administrativeDivision.id,
            enrollmentPeriodId = enrollmentPeriod.id
        )
        val paymentModel1 = MembershipPaymentModelFactory.create(
            membershipPaymentDao, householdEnrollmentRecordId = householdEnrollmentRecordModel.id)
        val paymentModel2 = MembershipPaymentModelFactory.create(
            membershipPaymentDao, householdEnrollmentRecordId = householdEnrollmentRecordModel.id)

        membershipPaymentDao.findByHousehold(householdModel.id).test().assertValue(
            listOf(paymentModel1, paymentModel2))
    }
}

package org.watsi.enrollment.device.db.daos

import org.junit.Test
import org.threeten.bp.LocalDate
import org.watsi.enrollment.device.factories.EnrollmentPeriodModelFactory

class EnrollmentPeriodDaoTest : DaoBaseTest() {

    @Test
    fun current() {
        val month1 = LocalDate.now().minusMonths(9)
        val month2 = LocalDate.now().minusMonths(8)
        val month3 = LocalDate.now().minusMonths(7)
        val month4 = LocalDate.now().minusMonths(6)
        val month5 = LocalDate.now().minusMonths(5)
        val month6 = LocalDate.now().minusMonths(4)
        val month7 = LocalDate.now().minusMonths(3)
        val month8 = LocalDate.now().minusMonths(2)
        val month9 = LocalDate.now().minusMonths(1)
        val month10 = LocalDate.now()
        val month11 = LocalDate.now().minusMonths(1)

        val ep1 = EnrollmentPeriodModelFactory.create(
            enrollmentPeriodDao = enrollmentPeriodDao,
            id = 1,
            startDate = month2,
            endDate = month4
        )

        val ep2 = EnrollmentPeriodModelFactory.create(
            enrollmentPeriodDao = enrollmentPeriodDao,
            id = 2,
            startDate = month4,
            endDate = month6
        )

        val ep3 = EnrollmentPeriodModelFactory.create(
            enrollmentPeriodDao = enrollmentPeriodDao,
            id = 3,
            startDate = month8,
            endDate = month10
        )

        enrollmentPeriodDao.current(month1).test().assertValue(emptyList())
        enrollmentPeriodDao.current(month2).test().assertValue(listOf(ep1))
        enrollmentPeriodDao.current(month3).test().assertValue(listOf(ep1))

        enrollmentPeriodDao.current(month4).test().assertValue(listOf(ep2))
        enrollmentPeriodDao.current(month5).test().assertValue(listOf(ep2))
        enrollmentPeriodDao.current(month6).test().assertValue(listOf(ep2))
        enrollmentPeriodDao.current(month7).test().assertValue(listOf(ep2))

        enrollmentPeriodDao.current(month8).test().assertValue(listOf(ep3))
        enrollmentPeriodDao.current(month9).test().assertValue(listOf(ep3))
        enrollmentPeriodDao.current(month10).test().assertValue(listOf(ep3))
        enrollmentPeriodDao.current(month11).test().assertValue(listOf(ep3))
    }
}

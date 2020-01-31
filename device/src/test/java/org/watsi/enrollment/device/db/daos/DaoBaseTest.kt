package org.watsi.enrollment.device.db.daos

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.persistence.room.Room
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.watsi.enrollment.device.db.AppDatabase

@RunWith(RobolectricTestRunner::class)
abstract class DaoBaseTest {
    lateinit var database: AppDatabase
    lateinit var householdDao: HouseholdDao
    lateinit var memberDao: MemberDao
    lateinit var photoDao: PhotoDao
    lateinit var membershipPaymentDao: MembershipPaymentDao
    lateinit var householdEnrollmentRecordDao: HouseholdEnrollmentRecordDao
    lateinit var deltaDao: DeltaDao
    lateinit var summaryStatsDao: SummaryStatsDao
    lateinit var enrollmentPeriodDao: EnrollmentPeriodDao
    lateinit var administrativeDivisionDao: AdministrativeDivisionDao

    // Instantly execute all DB operations in Dao tests
    @Rule @JvmField val rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUpDatabase() {
        database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.application.baseContext, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        householdDao = database.householdDao()
        memberDao = database.memberDao()
        photoDao = database.photoDao()
        membershipPaymentDao = database.membershipPaymentDao()
        householdEnrollmentRecordDao = database.householdEnrollmentRecordDao()
        deltaDao = database.deltaDao()
        summaryStatsDao = database.summaryStatsDao()
        enrollmentPeriodDao = database.enrollmentPeriodDao()
        administrativeDivisionDao = database.administrativeDivisionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }
}

package org.watsi.enrollment.device.repositories

import android.database.sqlite.SQLiteException
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.enrollment.device.api.CoverageApi
import org.watsi.enrollment.device.db.daos.DeltaDao
import org.watsi.enrollment.device.db.daos.HouseholdDao
import org.watsi.enrollment.device.db.daos.MemberDao
import org.watsi.enrollment.device.db.models.HouseholdEnrollmentRecordWithPaymentsModel
import org.watsi.enrollment.device.db.models.HouseholdWithMembersAndPaymentsModel
import org.watsi.enrollment.device.db.models.HouseholdWithMembersModel
import org.watsi.enrollment.device.factories.AdministrativeDivisionModelFactory
import org.watsi.enrollment.device.factories.EnrollmentPeriodModelFactory
import org.watsi.enrollment.device.factories.HouseholdEnrollmentRecordModelFactory
import org.watsi.enrollment.device.factories.HouseholdModelFactory
import org.watsi.enrollment.device.factories.MemberModelFactory
import org.watsi.enrollment.device.factories.MemberWithThumbnailModelFactory
import org.watsi.enrollment.device.factories.MembershipPaymentModelFactory
import org.watsi.enrollment.device.managers.PreferencesManager
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.factories.HouseholdFactory
import org.watsi.enrollment.domain.factories.HouseholdWithMembersAndPaymentsFactory
import org.watsi.enrollment.domain.factories.MemberWithThumbnailFactory

@RunWith(MockitoJUnitRunner::class)
class HouseholdRepositoryImplTest {
    @Mock lateinit var mockHouseholdDao: HouseholdDao
    @Mock lateinit var deltaDao: DeltaDao
    @Mock lateinit var memberDao: MemberDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    @Mock lateinit var mockPreferencesManager: PreferencesManager
    @Mock lateinit var okHttpClient: OkHttpClient

    lateinit var householdRepo: HouseholdRepositoryImpl
    val now = Instant.now()
    val clock = Clock.fixed(now, ZoneId.systemDefault())
    val enrollmentPeriodModel = EnrollmentPeriodModelFactory.build()
    val administrativeDivisionModel = AdministrativeDivisionModelFactory.build()
    val householdModel = HouseholdModelFactory.build(administrativeDivisionId = administrativeDivisionModel.id)

    @Before
    fun setup() {
        //whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        householdRepo = HouseholdRepositoryImpl(
                mockHouseholdDao, deltaDao, memberDao, mockApi, mockSessionManager, clock, okHttpClient, mockPreferencesManager)
    }

    @Test
    fun get_noHouseholdsArePersisted_returnsEmpty() {
        whenever(mockHouseholdDao.getFlowable(householdModel.id))
                .thenReturn(Flowable.empty())
        householdRepo.get(householdModel.id).test().assertResult()
    }

    @Test
    fun get_householdIsPersisted_returnsHouseholdWithMembersAndPayments() {
        val memberModel1 = MemberModelFactory.build(householdId = householdModel.id)
        val memberModel2 = MemberModelFactory.build(householdId = householdModel.id)
        val memberWithPhotoModel1 = MemberWithThumbnailModelFactory.build(memberModel1)
        val memberWithPhotoModel2 = MemberWithThumbnailModelFactory.build(memberModel2)
        val householdEnrollmentRecordModel = HouseholdEnrollmentRecordModelFactory.build(
            householdId = householdModel.id,
            administrativeDivisionId = administrativeDivisionModel.id,
            enrollmentPeriodId = enrollmentPeriodModel.id
        )
        val paymentModel = MembershipPaymentModelFactory.build(householdEnrollmentRecordId = householdEnrollmentRecordModel.id)
        val householdEnrollmentRecordWithPaymentModel =
                HouseholdEnrollmentRecordWithPaymentsModel(householdEnrollmentRecordModel, listOf(paymentModel))
        val householdWithMembersAndPaymentsModel = HouseholdWithMembersAndPaymentsModel(
            householdModel,
            listOf(householdEnrollmentRecordWithPaymentModel),
            listOf(memberWithPhotoModel1, memberWithPhotoModel2)
        )

        val memberEntity1 = memberModel1.toMember()
        val memberEntity2 = memberModel2.toMember()
        val memberWithPhotoEntity1 = MemberWithThumbnailFactory.build(memberEntity1)
        val memberWithPhotoEntity2 = MemberWithThumbnailFactory.build(memberEntity2)
        val householdEnrollmentRecordEntity = householdEnrollmentRecordModel.toHouseholdEnrollmentRecord()
        val paymentEntity = paymentModel.toMembershipPayment()
        val household = HouseholdFactory.build(
            id = householdModel.id,
            enrolledAt = householdModel.enrolledAt,
            administrativeDivisionId = householdModel.administrativeDivisionId
        )
        val householdWithMembersAndPaymentsEntity = HouseholdWithMembersAndPaymentsFactory.build(
            household = household,
            householdEnrollmentRecords = listOf(householdEnrollmentRecordEntity),
            members = listOf(memberWithPhotoEntity1, memberWithPhotoEntity2),
            payments = listOf(paymentEntity)
        )

        whenever(mockHouseholdDao.getFlowable(householdModel.id))
                .thenReturn(Flowable.just(householdWithMembersAndPaymentsModel))
        householdRepo.get(householdModel.id).test().assertResult(householdWithMembersAndPaymentsEntity)
    }

    @Test
    fun get_throwsException_callsOnError() {
        whenever(mockHouseholdDao.getFlowable(householdModel.id))
                .thenReturn(Flowable.error(SQLiteException()))
        householdRepo.get(householdModel.id).test().assertFailure(SQLiteException::class.java)
    }

    @Test
    fun createdOrEditedAfter_returnsHouseholds() {
        val householdModel = HouseholdModelFactory.build(administrativeDivisionId = administrativeDivisionModel.id)
        val householdWithMemberModel = HouseholdWithMembersModel(householdModel, emptyList())
        whenever(mockHouseholdDao.createdOrEditedAfter(now))
                .thenReturn(Flowable.just(listOf(householdWithMemberModel)))

        householdRepo.createdOrEditedAfter(now).test().assertValue(
                listOf(householdWithMemberModel.toHouseholdWithMembers()))
    }

    @Test
    fun createdOrEditedAfter_throwsException_callsOnError() {
        whenever(mockHouseholdDao.createdOrEditedAfter(any())).thenReturn(Flowable.error(SQLiteException()))
        householdRepo.createdOrEditedAfter(now).test().assertFailure(SQLiteException::class.java)
    }

    @Test
    fun save_householdDaothrowsException_callsOnError() {
        val householdEntity = HouseholdFactory.build()

        whenever(mockHouseholdDao.insertWithDelta(any(), any())).thenThrow(SQLiteException())
        householdRepo.save(householdEntity).test().assertFailure(SQLiteException::class.java)
    }

//    @Test
//    fun fetch_isSuccessful_completes() {
//        val householdWithMembers = HouseholdWithMembersAndPaymentsFactory.build()
//        whenever(mockApi.getHousehold(any(), any())).thenReturn(Single.just(householdWithMembers))
//
//        householdRepo.fetch("card_id").test().assertResult(householdWithMembers)
//    }
//
//    @Test
//    fun fetch_throwsException_callsOnError() {
//        val exception = SocketTimeoutException()
//        whenever(mockApi.getHousehold(any(), any())).thenReturn(Single.error(exception))
//
//        householdRepo.fetch("card_id").test().assertError(exception)
//    }

//    @Test
//    fun sync() {
//    }
}

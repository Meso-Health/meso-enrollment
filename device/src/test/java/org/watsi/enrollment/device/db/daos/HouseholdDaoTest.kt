package org.watsi.enrollment.device.db.daos

import android.database.sqlite.SQLiteException
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.enrollment.device.db.models.AdministrativeDivisionModel
import org.watsi.enrollment.device.db.models.EnrollmentPeriodModel
import org.watsi.enrollment.device.db.models.HouseholdEnrollmentRecordModel
import org.watsi.enrollment.device.db.models.HouseholdEnrollmentRecordWithPaymentsModel
import org.watsi.enrollment.device.db.models.HouseholdModel
import org.watsi.enrollment.device.db.models.HouseholdWithMembersAndPaymentsModel
import org.watsi.enrollment.device.db.models.HouseholdWithMembersModel
import org.watsi.enrollment.device.db.models.MemberWithThumbnailModel
import org.watsi.enrollment.device.factories.AdministrativeDivisionModelFactory
import org.watsi.enrollment.device.factories.DeltaModelFactory
import org.watsi.enrollment.device.factories.EnrollmentPeriodModelFactory
import org.watsi.enrollment.device.factories.HouseholdEnrollmentRecordModelFactory
import org.watsi.enrollment.device.factories.HouseholdModelFactory
import org.watsi.enrollment.device.factories.MemberModelFactory
import org.watsi.enrollment.device.factories.MembershipPaymentModelFactory
import org.watsi.enrollment.domain.entities.Delta
import java.util.UUID

class HouseholdDaoTest : DaoBaseTest() {

    lateinit var administrativeDivisionModel: AdministrativeDivisionModel
    lateinit var enrollmentPeriodModel: EnrollmentPeriodModel
    lateinit var now: Instant
    lateinit var clock: Clock

    @Before
    fun setup() {
        administrativeDivisionModel = AdministrativeDivisionModelFactory.create(administrativeDivisionDao)
        enrollmentPeriodModel = EnrollmentPeriodModelFactory.create(enrollmentPeriodDao)
        now = Instant.now()
        clock = Clock.fixed(now, ZoneId.systemDefault())
    }

    @Test
    fun createdOrEditedAfter_returnsAllHouseholdsEnrolledAfterTheGivenTimeOrderedByDecreasingEnrolledAt() {
        createHouseholdEnrolledAt(now.minusSeconds(10000))
        val h2 = createHouseholdEnrolledAt(now.plusSeconds(20000))
        val h3 = createHouseholdEnrolledAt(now.plusSeconds(30000))
        val h4WithMemberAddedRecently = createHouseholdEnrolledAt(now.minusSeconds(40000))
        val h5WithMemberEdit = createHouseholdEnrolledAt(now.minusSeconds(50000))
        val h6WithEnrollmentRecord = createHouseholdEnrolledAt(now.minusSeconds(60000))
        val h7WithPenalty = createHouseholdEnrolledAt(now.minusSeconds(70000))

        createHouseholdEnrollmentRecordEnrolledAt(
            householdId = h6WithEnrollmentRecord.id,
            enrolledAt = now.plusSeconds(200000)
        )
        val h7EnrollmentRecord = createHouseholdEnrollmentRecordEnrolledAt(
            householdId = h7WithPenalty.id,
            enrolledAt = now.minusSeconds(10000)
        )
        val h7Penalty = MembershipPaymentModelFactory.create(membershipPaymentDao, householdEnrollmentRecordId = h7EnrollmentRecord.id, createdAt = now.plusSeconds(300000))
        DeltaModelFactory.create(deltaDao, modelId = h7Penalty.id, action = Delta.Action.ADD, modelName = Delta.ModelName.MEMBERSHIP_PAYMENT, createdAt = now.plusSeconds(300000), clock = clock)

        val memberModel = MemberModelFactory.create(memberDao, householdId = h4WithMemberAddedRecently.id, enrolledAt = now.plusSeconds(50000), clock = clock)
        val memberModel2 = MemberModelFactory.create(memberDao, householdId = h5WithMemberEdit.id, enrolledAt = now.minusSeconds(60000), clock = clock)
        val memberModel3 = MemberModelFactory.create(memberDao, householdId = h5WithMemberEdit.id, enrolledAt = now.minusSeconds(60000), clock = clock)
        DeltaModelFactory.create(deltaDao, modelId = memberModel2.id, action = Delta.Action.EDIT, modelName = Delta.ModelName.MEMBER, createdAt = now.plusSeconds(100000), clock = clock)
        val h2WithMembers = HouseholdWithMembersModel(h2, emptyList(), listOf(administrativeDivisionModel))
        val h3WithMembers = HouseholdWithMembersModel(h3, emptyList(), listOf(administrativeDivisionModel))
        val h4WithMembers = HouseholdWithMembersModel(h4WithMemberAddedRecently, listOf(MemberWithThumbnailModel(memberModel = memberModel, photoModels = emptyList())), listOf(administrativeDivisionModel))
        val h5WithMembers = HouseholdWithMembersModel(h5WithMemberEdit, listOf(MemberWithThumbnailModel(memberModel = memberModel2, photoModels = emptyList()), MemberWithThumbnailModel(memberModel = memberModel3, photoModels = emptyList())), listOf(administrativeDivisionModel))
        val h6WithMembers = HouseholdWithMembersModel(h6WithEnrollmentRecord, emptyList(), listOf(administrativeDivisionModel))
        val h7WithMembers = HouseholdWithMembersModel(h7WithPenalty, emptyList(), listOf(administrativeDivisionModel))

        householdDao.createdOrEditedAfter(now).test().assertValue(
            listOf(
                h7WithMembers,
                h6WithMembers,
                h5WithMembers,
                h4WithMembers,
                h3WithMembers,
                h2WithMembers
            )
        )
    }

    @Test
    fun get_householdDoesNotExist_returnsEmpty() {
        householdDao.getFlowable(UUID.randomUUID()).test().assertEmpty()
    }

    @Test
    fun get_householdExists_returnsHouseholdWithMembersAndPayments() {
        val h1 = createHouseholdEnrolledAt(now)
        val m1 = MemberWithThumbnailModel(MemberModelFactory.create(memberDao, householdId = h1.id), emptyList())
        val m2 = MemberWithThumbnailModel(MemberModelFactory.create(memberDao, householdId = h1.id), emptyList())
        val m3 = MemberWithThumbnailModel(MemberModelFactory.create(memberDao, householdId = h1.id), emptyList())

        val householdEnrollmentRecord =createHouseholdEnrollmentRecordEnrolledAt(householdId = h1.id, enrolledAt = now)
        val payment = MembershipPaymentModelFactory.create(membershipPaymentDao, householdEnrollmentRecordId = householdEnrollmentRecord.id)
        val householdEnrollmentRecordWithPaymentsModel = HouseholdEnrollmentRecordWithPaymentsModel(householdEnrollmentRecord, listOf(payment))

        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsModel(
            h1,
            listOf(householdEnrollmentRecordWithPaymentsModel),
            listOf(m1, m2, m3),
            listOf(administrativeDivisionModel)
        )

        householdDao.getFlowable(h1.id).test().assertValue(householdWithMembersAndPayments)
    }

    @Test
    fun insertWithDelta_success() {
        val h1 = HouseholdModelFactory.build(administrativeDivisionId = administrativeDivisionModel.id)
        val deltaModel = DeltaModelFactory.build(
            id = 1,
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.HOUSEHOLD,
            modelId = h1.id,
            synced = false
        )

        householdDao.insertWithDelta(h1, deltaModel)

        deltaDao.getAll().test().assertValue(listOf(deltaModel))
        householdDao.getFlowable(h1.id).test().assertValue(
            HouseholdWithMembersAndPaymentsModel(h1, emptyList(), emptyList(), listOf(administrativeDivisionModel)))
    }

    @Test(expected = SQLiteException::class)
    fun insertWithDelta_fail() {
        val h1 = createHouseholdEnrolledAt(now)
        val deltaModel = DeltaModelFactory.build(
            modelName = Delta.ModelName.HOUSEHOLD,
            synced = false
        )

        householdDao.insertWithDelta(h1, deltaModel)
    }

    private fun createHouseholdEnrolledAt(enrolledAt: Instant): HouseholdModel {
        return HouseholdModelFactory.create(
            householdDao,
            enrolledAt = enrolledAt,
            administrativeDivisionId = administrativeDivisionModel.id
        )
    }

    private fun createHouseholdEnrollmentRecordEnrolledAt(
        householdId: UUID,
        enrolledAt: Instant
    ): HouseholdEnrollmentRecordModel {
        return HouseholdEnrollmentRecordModelFactory.create(
            householdEnrollmentRecordDao,
            householdId = householdId,
            enrolledAt = enrolledAt,
            administrativeDivisionId = administrativeDivisionModel.id,
            enrollmentPeriodId = enrollmentPeriodModel.id,
            clock = clock
        )
    }
}

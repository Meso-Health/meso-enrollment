package org.watsi.enrollment.viewmodels

import android.arch.lifecycle.MutableLiveData
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.PaymentType
import org.watsi.enrollment.domain.factories.HouseholdFactory
import org.watsi.enrollment.domain.factories.HouseholdWithMembersAndPaymentsFactory
import org.watsi.enrollment.domain.factories.MemberFactory
import org.watsi.enrollment.domain.factories.MemberWithThumbnailFactory
import org.watsi.enrollment.domain.usecases.LoadCurrentEnrollmentPeriodUseCase
import org.watsi.enrollment.flowstates.HouseholdFlowState
import org.watsi.enrollment.testutils.AACBaseTest
import java.util.UUID

class PaymentViewModelTest : AACBaseTest() {
    @Mock lateinit var mockLogger: Logger
    @Mock lateinit var mockEnrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase

    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    val viewStateObserver = MutableLiveData<PaymentViewModel.ViewState>()
    val householdId = UUID.randomUUID()
    val adultBirthdate = LocalDate.now(clock).minusYears(20)
    val childBirthdate = LocalDate.now(clock).minusYears(2)

    val member = MemberFactory.build(id =  UUID.randomUUID())
    val spouseMember = MemberFactory.build(id =  UUID.randomUUID(), relationshipToHead = "spouse", birthdate = adultBirthdate)
    val additionalSpouseMember = MemberFactory.build(id =  UUID.randomUUID(), relationshipToHead = "spouse", birthdate = adultBirthdate)
    val disabledMember = MemberFactory.build(id =  UUID.randomUUID(), relationshipToHead = "child", profession = "disabled", birthdate = adultBirthdate)
    val paidMemberId = UUID.randomUUID()
    val activeBeneficiaryMember = MemberFactory.build(id = paidMemberId, birthdate = adultBirthdate)
    val unpaidMemberId = UUID.randomUUID()
    val unpaidBeneficiaryMember = MemberFactory.build(id =  unpaidMemberId, birthdate = adultBirthdate, archivedReason = Member.ARCHIVED_REASON_UNPAID, archivedAt = Instant.now())
    val underAgeMember = MemberFactory.build(id =  UUID.randomUUID(), birthdate = childBirthdate, relationshipToHead = "child")
    val membersWithThumbnail = listOf(
        MemberWithThumbnailFactory.build(member),
        MemberWithThumbnailFactory.build(spouseMember),
        MemberWithThumbnailFactory.build(additionalSpouseMember),
        MemberWithThumbnailFactory.build(disabledMember),
        MemberWithThumbnailFactory.build(activeBeneficiaryMember),
        MemberWithThumbnailFactory.build(unpaidBeneficiaryMember),
        MemberWithThumbnailFactory.build(underAgeMember)
    )
    val household = HouseholdFactory.build(id = householdId)
    val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
        household = household,
        members = membersWithThumbnail
    )

    val members = householdWithMembersAndPayments.members.map { it.member }.toMutableList()
    val householdFlowState = HouseholdFlowState(
        household = householdWithMembersAndPayments.household,
        householdEnrollmentRecords = householdWithMembersAndPayments.householdEnrollmentRecords,
        members = members,
        payments = householdWithMembersAndPayments.payments.toMutableList(),
        administrativeDivision = householdWithMembersAndPayments.administrativeDivision
    )

    val paymentType = PaymentType.NEW_ENROLLMENT
    val initialViewState = PaymentViewModel.ViewState(paymentType = paymentType)
    val validViewState = PaymentViewModel.ViewState(isPaying = true, paymentType = paymentType)
    lateinit var viewModel: PaymentViewModel

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        viewModel = PaymentViewModel(mockEnrollmentPeriodUseCase, clock, mockLogger)
        viewModel.householdFlowState = householdFlowState
        viewStateObserver.observeForever({})

        viewStateObserver.value = initialViewState
    }

    @Test
    fun init_reviewState() {
        val unpaidBeneficiary = viewModel.householdFlowState.members.find { it.id == unpaidMemberId }
        val paidBeneficiary = viewModel.householdFlowState.members.find { it.id == paidMemberId }
        assertEquals(unpaidBeneficiary?.archivedReason, Member.ARCHIVED_REASON_UNPAID)
        assertEquals(paidBeneficiary?.archivedReason, null)
    }

    @Test
    fun onBeneficiarySwitchToggled_unpaidIsChecked() {
        viewStateObserver.value = validViewState
        viewModel.onBeneficiarySwitchToggled(memberId = unpaidMemberId, isChecked = true)
        val toggledMember = viewModel.householdFlowState.members.find { it.id == unpaidMemberId }
        assertEquals(toggledMember?.archivedReason, null)
    }

    @Test
    fun onBeneficiarySwitchToggled_paidIsUnchecked() {
        viewModel.onBeneficiarySwitchToggled(memberId = paidMemberId, isChecked = false)
        val toggledMember = viewModel.householdFlowState.members.find { it.id == paidMemberId }
        assertEquals(toggledMember?.archivedReason, Member.ARCHIVED_REASON_UNPAID)
    }
}

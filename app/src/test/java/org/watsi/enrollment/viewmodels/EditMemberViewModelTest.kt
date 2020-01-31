package org.watsi.enrollment.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.entities.DateAccuracy
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.factories.EnrollmentPeriodFactory
import org.watsi.enrollment.domain.factories.HouseholdEnrollmentRecordFactory
import org.watsi.enrollment.domain.factories.HouseholdFactory
import org.watsi.enrollment.domain.factories.HouseholdWithMembersAndPaymentsFactory
import org.watsi.enrollment.domain.factories.MemberFactory
import org.watsi.enrollment.domain.factories.PhotoFactory
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import org.watsi.enrollment.domain.usecases.LoadCurrentEnrollmentPeriodUseCase
import org.watsi.enrollment.domain.usecases.LoadHouseholdUseCase
import org.watsi.enrollment.domain.usecases.UpdateMemberUseCase
import org.watsi.enrollment.testutils.AACBaseTest
import java.util.UUID

class EditMemberViewModelTest : AACBaseTest() {

    @Mock lateinit var mockLoadHouseholdUseCase: LoadHouseholdUseCase
    @Mock lateinit var mockUpdateMemberUseCase: UpdateMemberUseCase
    @Mock lateinit var mockEnrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase
    @Mock lateinit var mockViewState: EditMemberViewModel.ViewState
    @Mock lateinit var mockLiveData: LiveData<EditMemberViewModel.ViewState>
    @Mock lateinit var mockLogger: Logger
    val viewStateObservable = MediatorLiveData<EditMemberViewModel.ViewState>()
    val thumbnailPhotoId = UUID.randomUUID()
    val householdId = UUID.randomUUID()
    val member = MemberFactory.build(thumbnailPhotoId = thumbnailPhotoId, householdId = householdId)
    val thumbnailPhoto = PhotoFactory.build(id = thumbnailPhotoId)
    val memberWithThumbnail = MemberWithThumbnail(member, thumbnailPhoto)
    val household = HouseholdWithMembersAndPaymentsFactory.build(
        household = HouseholdFactory.build(householdId),
        householdEnrollmentRecords = listOf(HouseholdEnrollmentRecordFactory.build(paying = true)),
        members = listOf(memberWithThumbnail),
        payments = emptyList()
    )
    val enrollmentPeriod = EnrollmentPeriodFactory.build()
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    lateinit var viewModel: EditMemberViewModel

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        viewModel = EditMemberViewModel(
            mockLoadHouseholdUseCase,
            mockUpdateMemberUseCase,
            mockEnrollmentPeriodUseCase,
            viewStateObservable,
            mockLogger,
            clock
        )
        viewStateObservable.observeForever({})
    }

    @Test
    fun getObservable() {
        val viewModelSpy = spy(viewModel)
        doNothing().whenever(viewModelSpy).setLiveDataSource(any())

        assertEquals(viewModelSpy.getObservable(member), viewStateObservable)
        verify(viewModelSpy).setLiveDataSource(member)
    }

    @Test
    fun setLiveDataSource_sourceIsNotSet_setsSource() {
        val mockMediatorLiveData = mock<MediatorLiveData<EditMemberViewModel.ViewState>>()
        val viewModelSpy = spy(EditMemberViewModel(
                mockLoadHouseholdUseCase, mockUpdateMemberUseCase, mockEnrollmentPeriodUseCase, mockMediatorLiveData, mockLogger, clock))
        doReturn(mockLiveData).whenever(viewModelSpy).createLiveDataFromLoadMember(member)

        viewModelSpy.setLiveDataSource(member)

        assertEquals(viewModelSpy.sourceLiveData, mockLiveData)
        verify(mockMediatorLiveData).addSource(
                eq(mockLiveData), any<EditMemberViewModel.ViewStateObserver>())
    }

    @Test
    fun setLiveDataSource_sourceIsSet_removesAndResetsSource() {
        val mockInitialSource = mock<LiveData<EditMemberViewModel.ViewState>>()
        val mockMediatorLiveData = mock<MediatorLiveData<EditMemberViewModel.ViewState>>()
        val viewModelSpy = spy(EditMemberViewModel(
                mockLoadHouseholdUseCase, mockUpdateMemberUseCase, mockEnrollmentPeriodUseCase, mockMediatorLiveData, mockLogger, clock))
        viewModelSpy.sourceLiveData = mockInitialSource
        doReturn(mockLiveData).whenever(viewModelSpy).createLiveDataFromLoadMember(member)

        viewModelSpy.setLiveDataSource(member)

        verify(mockMediatorLiveData).removeSource(mockInitialSource)
        assertEquals(viewModelSpy.sourceLiveData, mockLiveData)
        verify(mockMediatorLiveData).addSource(
                eq(mockLiveData), any<EditMemberViewModel.ViewStateObserver>())
    }

    @Test
    fun createLiveDataFromLoadMember() {
        whenever(mockLoadHouseholdUseCase.execute(member.householdId))
                .thenReturn(Flowable.just(household))
        whenever(mockEnrollmentPeriodUseCase.executeFlowable()).thenReturn(Flowable.just(enrollmentPeriod))

        val liveData = viewModel.createLiveDataFromLoadMember(member)

        liveData.observeForever({})
        assertEquals(liveData.value, EditMemberViewModel.ViewState(memberWithThumbnail, household, enrollmentPeriod))
    }

    @Test
    fun callIfMemberExists_viewStateIsNull_neverCompletes() {
        viewStateObservable.value = null
        val updateMember: (member: Member) -> Completable = { _ -> Completable.complete() }

        viewModel.callIfMemberExists(updateMember).test().assertNotComplete()
    }

    @Test
    fun callIfMemberExists_viewStateMemberIsNull_neverCompletes() {
        viewStateObservable.value = EditMemberViewModel.ViewState(null, null, null)
        val updateMember: (member: Member) -> Completable = { _ -> Completable.complete() }

        viewModel.callIfMemberExists(updateMember).test().assertNotComplete()
    }

    @Test
    fun callIfMemberExists_viewStateMemberIsNotNull_callsUpdateMember() {
        viewStateObservable.value = EditMemberViewModel.ViewState(memberWithThumbnail, household, enrollmentPeriod)
        val updateMember: (member: Member) -> Completable = { _ -> Completable.complete() }

        viewModel.callIfMemberExists(updateMember).test().assertComplete()
    }

    private fun updatedViewState(member: Member): EditMemberViewModel.ViewState {
        val updatedMemberWithThumbnail = memberWithThumbnail.copy(member = member)
        val updatedHousehold = household.copy(
            members = listOf(updatedMemberWithThumbnail)
        )
        return EditMemberViewModel.ViewState(updatedMemberWithThumbnail, updatedHousehold, enrollmentPeriod)
    }

    @Test
    fun updateName_callsUpdateMemberUseCaseWithUpdatedName() {
        val updatedName = "foo"
        viewStateObservable.value = updatedViewState(member.copy(name = "bar"))
        whenever(mockUpdateMemberUseCase.execute(member.copy(name = updatedName)))
                .thenReturn(Completable.complete())

        viewModel.updateName(updatedName).test().assertComplete()
    }

    @Test
    fun updateGender_callsUpdateMemberUseCaseWithUpdatedGender() {
        viewStateObservable.value = updatedViewState(member.copy(gender = Gender.M))
        whenever(mockUpdateMemberUseCase.execute(member.copy(gender = Gender.F)))
                .thenReturn(Completable.complete())

        viewModel.updateGender(Gender.F).test().assertComplete()
    }

    @Test
    fun updateBirthdate_callsUpdateMemberUseCaseWithUpdatedBirthdate() {
        val newBirthdate = LocalDate.now(clock).minusMonths(15)
        val newBirthdateAccuracy = DateAccuracy.M
        viewStateObservable.value = updatedViewState(member.copy(
                birthdate = LocalDate.now(clock), birthdateAccuracy = DateAccuracy.Y))
        whenever(mockUpdateMemberUseCase.execute(
                member.copy(birthdate = newBirthdate, birthdateAccuracy = newBirthdateAccuracy))
        ).thenReturn(Completable.complete())

        viewModel.updateBirthdate(newBirthdate, newBirthdateAccuracy).test().assertComplete()
    }

    @Test
    fun updatePhoneNumber_callsUpdateMemberUseCaseWithUpdatedPhoneNumber() {
        val phoneNumber = "0775888888"
        viewStateObservable.value = updatedViewState(member)
        whenever(mockUpdateMemberUseCase.execute(member.copy(phoneNumber = phoneNumber)))
                .thenReturn(Completable.complete())

        viewModel.updatePhoneNumber(phoneNumber).test().assertComplete()
    }

    @Test
    fun updatePhoneNumber_blankString_callsUpdateMemberUseCaseWithNullPhoneNumber() {
        viewStateObservable.value = updatedViewState(member)
        whenever(mockUpdateMemberUseCase.execute(member.copy(phoneNumber = null)))
                .thenReturn(Completable.complete())

        viewModel.updatePhoneNumber("").test().assertComplete()
    }

    @Test
    fun updatePhoto_callsUpdateMemberUseCaseWithPhotoIds() {
        val rawPhotoId = UUID.randomUUID()
        val thumbnailPhotoId = UUID.randomUUID()
        viewStateObservable.value = updatedViewState(member)
        whenever(mockUpdateMemberUseCase.execute(member.copy(photoId = rawPhotoId,
                thumbnailPhotoId = thumbnailPhotoId)))
                .thenReturn(Completable.complete())

        viewModel.updatePhoto(rawPhotoId, thumbnailPhotoId).test().assertComplete()
    }

    @Test
    fun updateMemberCard_callsUpdateMemberUseCaseWithMemberCardId() {
        val cardId = "RWI000000"
        viewStateObservable.value = updatedViewState(member)
        whenever(mockUpdateMemberUseCase.execute(member.copy(cardId = cardId)))
                .thenReturn(Completable.complete())

        viewModel.updateMemberCard(cardId).test().assertComplete()
    }

    @Test
    fun updateProfession_callsUpdateMemberUseCaseWithProfession() {
        viewStateObservable.value = updatedViewState(member)
        whenever(mockUpdateMemberUseCase.execute(member.copy(profession = "farmer")))
                .thenReturn(Completable.complete())

        viewModel.updateProfession("farmer").test().assertComplete()
    }

    @Test
    fun updateRelationshipToHead_callsUpdateMemberUseCaseWithRelationshipToHead() {
        val relationshipToHead = Member.RELATIONSHIP_TO_HEAD_SELF
        viewStateObservable.value = updatedViewState(member)
        whenever(mockUpdateMemberUseCase.execute(member.copy(relationshipToHead = relationshipToHead)))
                .thenReturn(Completable.complete())

        viewModel.updateRelationshipToHead(relationshipToHead).test().assertComplete()
    }

    @Test
    fun ViewStateObserver_onChanged() {
        val observer = viewModel.ViewStateObserver()

        observer.onChanged(mockViewState)

        assertEquals(mockViewState, viewStateObservable.value)
    }
}

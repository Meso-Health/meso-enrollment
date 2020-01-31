package org.watsi.enrollment.viewmodels

import android.arch.lifecycle.MutableLiveData
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.R
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.entities.DateAccuracy
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.factories.EnrollmentPeriodFactory
import org.watsi.enrollment.domain.factories.HouseholdFactory
import org.watsi.enrollment.domain.factories.HouseholdWithMembersAndPaymentsFactory
import org.watsi.enrollment.domain.factories.MemberFactory
import org.watsi.enrollment.domain.factories.MemberWithThumbnailFactory
import org.watsi.enrollment.domain.factories.PhotoFactory
import org.watsi.enrollment.domain.factories.UserFactory
import org.watsi.enrollment.domain.usecases.EnrollMemberUseCase
import org.watsi.enrollment.domain.usecases.LoadCurrentEnrollmentPeriodUseCase
import org.watsi.enrollment.domain.usecases.LoadPhotoUseCase
import org.watsi.enrollment.testutils.AACBaseTest
import org.watsi.enrollment.viewmodels.NewMemberViewModel.Companion.MEMBER_GENDER_ERROR
import org.watsi.enrollment.viewmodels.NewMemberViewModel.Companion.MEMBER_MEDICAL_RECORD_NUMBER_ERROR
import org.watsi.enrollment.viewmodels.NewMemberViewModel.Companion.MEMBER_NAME_ERROR
import org.watsi.enrollment.viewmodels.NewMemberViewModel.Companion.MEMBER_NAME_LENGTH_ERROR
import org.watsi.enrollment.viewmodels.NewMemberViewModel.Companion.SAVE_ERROR
import org.watsi.enrollment.viewmodels.NewMemberViewModel.FormValidator.formValidationErrors
import org.watsi.enrollment.viewmodels.NewMemberViewModel.MemberStatus
import org.watsi.enrollment.viewmodels.NewMemberViewModel.ViewState
import java.util.UUID

class NewMemberViewModelTest : AACBaseTest() {
    @Mock lateinit var mockEnrollMemberUseCase: EnrollMemberUseCase
    @Mock lateinit var mockLoadPhotoUseCase: LoadPhotoUseCase
    @Mock lateinit var mockEnrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase
    @Mock lateinit var mockFormValidator: NewMemberViewModel.FormValidator
    @Mock lateinit var mockLogger: Logger

    val viewStateObserver = MutableLiveData<NewMemberViewModel.ViewState>()
    val householdId = UUID.randomUUID()
    val memberId = UUID.randomUUID()
    val household = HouseholdWithMembersAndPaymentsFactory.build(
        household = HouseholdFactory.build(id = householdId),
        members = listOf(MemberWithThumbnailFactory.build(MemberFactory.build(id = memberId)))
    )

    val photoId = UUID.randomUUID()
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    val user = UserFactory.build()
    val initialViewState = ViewState()
    val validViewState = ViewState(
            name = "Three Name Here",
            birthdate = LocalDate.now(clock),
            birthdateAccuracy = DateAccuracy.Y,
            gender = Gender.F,
            phoneNumber = null,
            photoId = photoId,
            cardId = null,
            profession = "farmer",
            relationshipToHead = Member.RELATIONSHIP_TO_HEAD_SELF)
    lateinit var viewModel: NewMemberViewModel

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        viewModel = NewMemberViewModel(
            mockEnrollMemberUseCase,
            mockLoadPhotoUseCase,
            mockEnrollmentPeriodUseCase,
            mockLogger,
            viewStateObserver,
            clock
        )
        viewStateObserver.observeForever({})
    }

    @Test
    fun init_newMemberState() {
        assertEquals(viewStateObserver.value, initialViewState)
    }

    @Test
    fun saveMember_statusNotSaving_enrollsMember() {
        viewStateObserver.value = validViewState
        whenever(mockEnrollMemberUseCase.execute(any(), any(), any(), any(), any()))
                .thenReturn(Completable.complete())
        whenever(mockFormValidator.formValidationErrors(any())).thenReturn(emptyMap())
        val enrollmentPeriod = EnrollmentPeriodFactory.build()
        whenever(mockEnrollmentPeriodUseCase.executeSingle()).thenReturn(Single.just(enrollmentPeriod))

        viewModel.saveMember(memberId, household, user, mockFormValidator)
            .test().assertComplete()

        assertEquals(validViewState.copy(status = MemberStatus.SAVING), viewStateObserver.value)
    }

    @Test
    fun saveMember_viewStateIsNotValid_setsErrorStateAndNeverCompletes() {
        viewStateObserver.value = validViewState
        val errors = hashMapOf("ERROR_KEY" to R.string.generic_save_error)
        whenever(mockFormValidator.formValidationErrors(any())).thenReturn(errors)

        viewModel.saveMember(memberId, household, user, mockFormValidator)
            .test().assertError(NewMemberViewModel.ValidationException::class.java)

        verify(mockEnrollMemberUseCase, never()).execute(any(), any(), any(), any(), any())
        assertEquals(validViewState.copy(errors = errors, status = MemberStatus.ERROR),
            viewStateObserver.value)
    }

    @Test
    fun saveMember_statusAlreadySaving_doesNothing() {
        viewStateObserver.value = validViewState.copy(status = MemberStatus.SAVING)

        viewModel.saveMember(memberId, household, user, mockFormValidator)
            .test().assertNotComplete()

        verify(mockFormValidator, never()).formValidationErrors(any())
        verify(mockEnrollMemberUseCase, never()).execute(any(), any(), any(), any(), any())
        assertEquals(validViewState.copy(status = MemberStatus.SAVING), viewStateObserver.value)
    }

    @Test
    fun saveMember_useCaseCallsOnError_setErrorStatusAndNeverCompletes() {
        viewStateObserver.value = validViewState
        whenever(mockFormValidator.formValidationErrors(any())).thenReturn(emptyMap())
        val exceptionMessage = "foo"
        val enrollmentPeriod = EnrollmentPeriodFactory.build()
        whenever(mockEnrollMemberUseCase.execute(any(), any(), any(), any(), any()))
                .thenReturn(Completable.error(Exception(exceptionMessage)))
        whenever(mockEnrollmentPeriodUseCase.executeSingle())
                .thenReturn(Single.just(enrollmentPeriod))

        viewModel.saveMember(memberId, household, user, mockFormValidator)
            .test().assertNotComplete()

        assertEquals(validViewState.copy(errors = mapOf(SAVE_ERROR to R.string.generic_save_error),
            status = MemberStatus.ERROR), viewStateObserver.value)
    }

    @Test
    fun formValidationErrors_multipleFieldsInvalid_returnsErrors() {
        assertEquals(formValidationErrors(validViewState.copy(
                name = "",
                gender = null,
                cardId = null)),
                hashMapOf(MEMBER_NAME_ERROR to R.string.name_validation_error,
                        MEMBER_GENDER_ERROR to R.string.gender_validation_error))

        assertEquals(formValidationErrors(validViewState.copy(name = "", gender = null)),
                hashMapOf(MEMBER_NAME_ERROR to R.string.name_validation_error,
                        MEMBER_GENDER_ERROR to R.string.gender_validation_error))

        assertEquals(formValidationErrors(validViewState.copy(name = "", cardId = null)),
                hashMapOf(MEMBER_NAME_ERROR to R.string.name_validation_error))
    }

    @Test
    fun formValidationErrors_noCardId_noError() {
        assertEquals(formValidationErrors(validViewState.copy(cardId = null)), emptyMap<String, Int>())
    }

    @Test
    fun formValidationErrors_noGender_returnsError() {
        assertEquals(formValidationErrors(validViewState.copy(gender = null)),
                hashMapOf(MEMBER_GENDER_ERROR to R.string.gender_validation_error))
    }

    @Test
    fun formValidationErrors_invalidName_returnsError() {
        assertEquals(formValidationErrors(validViewState.copy(name = "")),
            hashMapOf(MEMBER_NAME_ERROR to R.string.name_validation_error))

        assertEquals(formValidationErrors(validViewState.copy(name = "   ")),
            hashMapOf(MEMBER_NAME_ERROR to R.string.name_validation_error))

        if (BuildConfig.MEMBER_FULL_NAME_MIN_LENGTH == 3) {
            assertEquals(formValidationErrors(validViewState.copy(name = "Michael")),
                hashMapOf(MEMBER_NAME_LENGTH_ERROR to R.string.name_length_validation_error))
        } else {
            assertEquals(formValidationErrors(validViewState.copy(name = "Michael")),
                emptyMap<String, Int>())
        }

        assertEquals(formValidationErrors(validViewState.copy(name = "Michael B. Jordan")),
            emptyMap<String, Int>())
    }

    @Test
    fun formValidationErrors_noMedicalRecordNumber_noError() {
        assertEquals(formValidationErrors(validViewState.copy(medicalRecordNumber = null)), emptyMap<String, Int>())
    }

    @Test
    fun formValidationErrors_invalidMedicalRecordNumber_returnsErrorIfEnabled() {
        if (BuildConfig.MEMBER_MEDICAL_RECORD_NUMBER_MIN_LENGTH == 5) {
            assertEquals(formValidationErrors(validViewState.copy(medicalRecordNumber = "1234")),
                hashMapOf(MEMBER_MEDICAL_RECORD_NUMBER_ERROR to R.string.medical_record_number_validation_error))
        } else {
            assertEquals(formValidationErrors(validViewState.copy(medicalRecordNumber = "1234")),
                emptyMap<String, String>())
        }
    }

    @Test
    fun formValidationErrors_validViewState_noError() {
        assertEquals(formValidationErrors(validViewState), emptyMap<String, String>())

        assertEquals(formValidationErrors(validViewState.copy(phoneNumber = "1234567")),
                emptyMap<String, String>())
    }

    @Test
    fun onPhotoTaken_successfulFetch() {
        val photo = PhotoFactory.build()
        val croppedPhoto = PhotoFactory.build(UUID.randomUUID())
        whenever(mockLoadPhotoUseCase.execute(croppedPhoto.id)).thenReturn(Single.just(croppedPhoto))
        viewModel.onPhotoTaken(photo.id, croppedPhoto.id)
        assertEquals(viewStateObserver.value?.photoId, photo.id)
        assertEquals(viewStateObserver.value?.thumbnailPhoto, croppedPhoto)
    }

    @Test
    fun onPhotoTaken_failedFetch() {
        val photo = PhotoFactory.build()
        val croppedPhoto = PhotoFactory.build(UUID.randomUUID())
        val exception = Exception("oh no this is bad we failed to fetch thumbnail from DB.")
        whenever(mockLoadPhotoUseCase.execute(croppedPhoto.id)).thenReturn(Single.error(exception))
        viewModel.onPhotoTaken(photo.id, croppedPhoto.id)
        assertEquals(viewStateObserver.value?.photoId, photo.id)
        verify(mockLogger).error(exception)
        assertNull(viewStateObserver.value?.thumbnailPhoto)
    }

    @Test
    fun onPhoneNumberChange_nonEmptyString_setsPhoneNumber() {
        val phoneNumber = "0775888888"

        viewModel.onPhoneNumberChange(phoneNumber)

        assertEquals(viewStateObserver.value?.phoneNumber, phoneNumber)
    }

    @Test
    fun onPhoneNumberChange_emptyString_setsPhoneNumberAsNull() {
        viewModel.onPhoneNumberChange("")

        assertEquals(viewStateObserver.value?.phoneNumber, null)
    }

    @Test
    fun onRelationshipToHeadChange_nonDefault_setsRelationship() {
        val selectedRelationshipToHead = "grand child"
        viewModel.onRelationshipToHeadSelected(selectedRelationshipToHead)

        assertEquals(viewStateObserver.value?.relationshipToHead, selectedRelationshipToHead)
    }

    @Test
    fun onProfessionChange_nonDefault_setsProfession() {
        val selectedProfession = "student"
        viewModel.onProfessionSelected(selectedProfession)

        assertEquals(viewStateObserver.value?.profession, selectedProfession)
    }
}

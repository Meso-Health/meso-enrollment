package org.watsi.enrollment.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.R
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.entities.DateAccuracy
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.Photo
import org.watsi.enrollment.domain.entities.User
import org.watsi.enrollment.domain.relations.HouseholdWithMembersAndPayments
import org.watsi.enrollment.domain.usecases.EnrollMemberUseCase
import org.watsi.enrollment.domain.usecases.LoadCurrentEnrollmentPeriodUseCase
import org.watsi.enrollment.domain.usecases.LoadPhotoUseCase
import org.watsi.enrollment.flowstates.HouseholdFlowState
import java.util.UUID
import javax.inject.Inject

class NewMemberViewModel(
        private val enrollMemberUseCase: EnrollMemberUseCase,
        private val loadMemberPhotoUseCase: LoadPhotoUseCase,
        private val loadCurrentEnrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase,
        private val logger: Logger,
        private val viewStateObservable: MutableLiveData<ViewState>,
        private val clock: Clock
) : ViewModel() {

    @Inject constructor(
        enrollMemberUseCase: EnrollMemberUseCase,
        loadMemberPhotoUseCase: LoadPhotoUseCase,
        enrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase,
        logger: Logger,
        clock: Clock
    ) : this(enrollMemberUseCase, loadMemberPhotoUseCase, enrollmentPeriodUseCase, logger, MutableLiveData<ViewState>(), clock)

    init {
        viewStateObservable.value = ViewState()
    }

    fun cacheMember(householdFlowState: HouseholdFlowState, memberIndex: Int): HouseholdFlowState {
        viewStateObservable.value?.let {
            val newMember = toMember(
                viewState = it,
                memberId = UUID.randomUUID(),
                householdId = householdFlowState.household.id,
                isHeadOfHousehold = memberIndex == 0,
                membershipNumber = householdFlowState.manualMembershipNumber,
                clock = clock
            )

            if (householdFlowState.members.size > memberIndex) {
                householdFlowState.members[memberIndex] = newMember
            } else {
                householdFlowState.members.add(newMember)
            }

            return householdFlowState
        }
        throw java.lang.IllegalStateException("ViewStateToEntityMapper.toHouseholdFlowState should only be called with a valid viewState. " + viewStateObservable.toString())

    }

    fun saveMember(memberId: UUID,
                   household: HouseholdWithMembersAndPayments,
                   user: User,
                   formValidator: FormValidator = FormValidator) : Completable {
        val viewState = viewStateObservable.value
        if (viewState == null || viewState.status == MemberStatus.SAVING) {
            return Completable.never()
        }
        val validationErrors = formValidator.formValidationErrors(viewState)
        if (validationErrors.isNotEmpty()) {
            viewStateObservable.value = viewState.copy(status = MemberStatus.ERROR, errors = validationErrors)
            return Completable.error(ValidationException("Some are missing", validationErrors))
        }

        viewStateObservable.value = viewState.copy(status = MemberStatus.SAVING)
        val member = toMember(
            viewState = viewState,
            memberId = memberId,
            householdId = household.household.id,
            isHeadOfHousehold = viewState.relationshipToHead == Member.RELATIONSHIP_TO_HEAD_SELF,
            membershipNumber = null,
            clock = clock
        )

        return loadCurrentEnrollmentPeriodUseCase.executeSingle().flatMapCompletable { enrollmentPeriod ->
            enrollMemberUseCase.execute(member, household, enrollmentPeriod, user, clock)
                    .doOnError { onSaveError(it) }
                    .onErrorResumeNext { Completable.never() }
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun onNameChange(name: String) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { listOf(MEMBER_NAME_ERROR, MEMBER_NAME_LENGTH_ERROR).contains(it.key) }
            viewStateObservable.value = it.copy(name = name, errors = errors)
        }
    }

    fun onBirthdateChange(birthdate: LocalDate) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key == MEMBER_AGE_ERROR }
            viewStateObservable.value = it.copy(birthdate = birthdate, errors = errors)
        }
    }

    fun onBirthdateAccuracyChange(birthdateAccuracy: DateAccuracy) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key == MEMBER_AGE_ERROR }
            viewStateObservable.value = it.copy(birthdateAccuracy = birthdateAccuracy, errors = errors)
        }
    }

    fun onPhoneNumberChange(phoneNumber: String) {
        val nonEmptyPhoneNumber = if (phoneNumber.isBlank()) null else phoneNumber
        viewStateObservable.value = viewStateObservable.value?.copy(phoneNumber = nonEmptyPhoneNumber)
    }

    fun onMedicalRecordNumberChange(medicalRecordNumber: String) {
        viewStateObservable.value?.let {
            val nonEmptyMedicalRecordNumber = if (medicalRecordNumber.isBlank()) null else medicalRecordNumber
            val errors = it.errors.filterNot { it.key == MEMBER_MEDICAL_RECORD_NUMBER_ERROR }
            viewStateObservable.value = it.copy(medicalRecordNumber = nonEmptyMedicalRecordNumber, errors = errors)
        }
    }

    fun onGenderChange(gender: Gender) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key == MEMBER_GENDER_ERROR }
            viewStateObservable.value = it.copy(gender = gender, errors = errors)
        }
    }

    fun onCardScan(cardId: String) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(cardId = cardId)
        }
    }

    fun onProfessionSelected(profession: String?) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key == MEMBER_PROFESSION_ERROR }
            viewStateObservable.value = it.copy(profession = profession, errors = errors)
        }
    }

    fun onRelationshipToHeadSelected(relationshipToHead: String?) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key == MEMBER_RELATIONSHIP_TO_HEAD_ERROR }
            viewStateObservable.value = it.copy(relationshipToHead = relationshipToHead, errors = errors)
        }
    }

    fun onPhotoTaken(photoId: UUID, thumbnailPhotoId: UUID) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key == MEMBER_PHOTO_ERROR }
            viewStateObservable.value = it.copy(photoId = photoId, errors = errors)
        }
        loadMemberPhotoUseCase.execute(thumbnailPhotoId).subscribe(
            {
                    thumbnailPhoto ->
                viewStateObservable.postValue(viewStateObservable.value?.copy(thumbnailPhoto = thumbnailPhoto))
            },
            {  exception ->
                logger.error(exception)
            }
        )
    }

    fun getViewStateObservable(): LiveData<ViewState> = viewStateObservable

    private fun onSaveError(throwable: Throwable) {
        val errors = HashMap<String, Int>()
        errors[SAVE_ERROR] = R.string.generic_save_error
        logger.error(throwable, hashMapOf("viewState" to viewStateObservable.toString()))
        viewStateObservable.postValue(viewStateObservable.value?.copy(status = MemberStatus.ERROR, errors = errors))
    }

    fun errors(): Map<String, Int>? {
        return viewStateObservable.value?.errors
    }

    fun validateFields(): Completable {
        val viewState = viewStateObservable.value

        if (viewState == null) {
            return Completable.never()
        } else {
            val validationErrors = FormValidator.formValidationErrors(viewState)

            if (validationErrors.isNotEmpty()) {
                viewStateObservable.value = viewState.copy(errors = validationErrors)
                return Completable.error(Exception("Some fields are missing"))
            }
        }

        return Completable.complete()
    }

    object FormValidator {
        fun formValidationErrors(viewState: ViewState): Map<String, Int> {
            val errors = HashMap<String, Int>()

            if (viewState.name.isBlank()) {
                errors[MEMBER_NAME_ERROR] = R.string.name_validation_error
            } else if (!Member.isValidFullName(viewState.name, BuildConfig.MEMBER_FULL_NAME_MIN_LENGTH)) {
                errors[MEMBER_NAME_LENGTH_ERROR] = R.string.name_length_validation_error
            }

            if (viewState.gender == null) {
                errors[MEMBER_GENDER_ERROR] = R.string.gender_validation_error
            }

            if (viewState.birthdate == null && viewState.birthdateAccuracy == null) {
                errors[MEMBER_AGE_ERROR] = R.string.birthdate_validation_error
            }

            if (BuildConfig.ENABLE_COLLECT_RELATIONSHIP_TO_HEAD && viewState.relationshipToHead == null) {
                errors[MEMBER_RELATIONSHIP_TO_HEAD_ERROR] = R.string.relationship_to_member_validation_error
            }

            if (BuildConfig.ENABLE_COLLECT_PROFESSION && viewState.profession == null) {
                errors[MEMBER_PROFESSION_ERROR] = R.string.profession_validation_error
            }

            if (BuildConfig.ENABLE_COLLECT_MEDICAL_RECORD_NUMBER) {
                if (viewState.medicalRecordNumber != null &&
                        !Member.isValidMedicalRecordNumber(
                            medicalRecordNumber = viewState.medicalRecordNumber,
                            minLength = BuildConfig.MEMBER_MEDICAL_RECORD_NUMBER_MIN_LENGTH,
                            maxLength = BuildConfig.MEMBER_MEDICAL_RECORD_NUMBER_MAX_LENGTH
                        )
                ) {
                    errors[MEMBER_MEDICAL_RECORD_NUMBER_ERROR] = R.string.medical_record_number_validation_error
                }
            }

            return errors
        }
    }

    data class ViewState(val name: String = "",
                         val birthdate: LocalDate? = null,
                         val birthdateAccuracy: DateAccuracy? = null,
                         val gender: Gender? = null,
                         val phoneNumber: String? = null,
                         val medicalRecordNumber: String? = null,
                         val photoId: UUID? = null,
                         val thumbnailPhoto: Photo? = null,
                         val cardId: String? = null,
                         val isIndigent: Boolean = false,
                         val receiptNumber: String? = null,
                         val total: String? = null,
                         val profession: String? = null,
                         val relationshipToHead: String? = null,
                         val errors: Map<String, Int> = emptyMap(),
                         val status: MemberStatus = MemberStatus.NEW)

    enum class MemberStatus { ERROR, SAVING, NEW }

    data class ValidationException(val msg: String, val errors: Map<String, Int>): Exception(msg)

    companion object {
        const val SAVE_ERROR = "save_error"
        const val MEMBER_NAME_ERROR = "member_name_error"
        const val MEMBER_NAME_LENGTH_ERROR = "member_name_length_error"
        const val MEMBER_AGE_ERROR = "member_age_error"
        const val MEMBER_GENDER_ERROR = "member_gender_error"
        const val MEMBER_PHOTO_ERROR = "member_photo_error"
        const val MEMBER_RELATIONSHIP_TO_HEAD_ERROR = "member_relationship_to_head_error"
        const val MEMBER_PROFESSION_ERROR = "member_profession_error"
        const val MEMBER_MEDICAL_RECORD_NUMBER_ERROR = "member_medical_record_number_error"

        // This method should only be called `viewState` is valid. Otherwise, an exception would be thrown.
        fun toMember(
            viewState: ViewState,
            memberId: UUID,
            householdId: UUID,
            isHeadOfHousehold: Boolean,
            membershipNumber: String?,
            clock: Clock
        ): Member {
            if (FormValidator.formValidationErrors(viewState).isEmpty()) {
                return Member(
                    id = memberId,
                    enrolledAt = Instant.now(clock),
                    name = viewState.name,
                    birthdate = viewState.birthdate!!,
                    birthdateAccuracy = viewState.birthdateAccuracy!!,
                    gender = viewState.gender!!,
                    phoneNumber = viewState.phoneNumber,
                    photoId = viewState.photoId,
                    thumbnailPhotoId = viewState.thumbnailPhoto?.id,
                    cardId = viewState.cardId,
                    householdId = householdId,
                    profession = viewState.profession,
                    relationshipToHead = if (isHeadOfHousehold) Member.RELATIONSHIP_TO_HEAD_SELF else
                        viewState.relationshipToHead,
                    membershipNumber = membershipNumber,
                    medicalRecordNumber = viewState.medicalRecordNumber,
                    archivedAt = null,
                    archivedReason = null)
            } else {
                throw IllegalStateException("ViewStateToEntityMapper.fromMemberViewStateToMember should only be called with a valid viewState. " + viewState.toString())
            }
        }
    }
}

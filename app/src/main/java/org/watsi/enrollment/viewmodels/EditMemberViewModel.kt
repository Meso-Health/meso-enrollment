package org.watsi.enrollment.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.entities.DateAccuracy
import org.watsi.enrollment.domain.entities.EnrollmentPeriod
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.relations.HouseholdWithMembersAndPayments
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import org.watsi.enrollment.domain.usecases.LoadCurrentEnrollmentPeriodUseCase
import org.watsi.enrollment.domain.usecases.LoadHouseholdUseCase
import org.watsi.enrollment.domain.usecases.UpdateMemberUseCase
import java.util.UUID
import javax.inject.Inject

class EditMemberViewModel(
        private val loadHouseholdUseCase: LoadHouseholdUseCase,
        private val updateMemberUseCase: UpdateMemberUseCase,
        private val loadCurrentEnrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase,
        private val viewStateObservable: MediatorLiveData<ViewState>,
        private val logger: Logger,
        private val clock: Clock
) : ViewModel() {

    @Inject constructor(
        loadHouseholdUseCase: LoadHouseholdUseCase,
        updateMemberUseCase: UpdateMemberUseCase,
        enrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase,
        logger: Logger,
        clock: Clock
    ) : this(loadHouseholdUseCase, updateMemberUseCase, enrollmentPeriodUseCase, MediatorLiveData<ViewState>(), logger, clock)

    internal var sourceLiveData: LiveData<ViewState>? = null

    data class ViewState(
        val memberWithThumbnail: MemberWithThumbnail?,
        val household: HouseholdWithMembersAndPayments?,
        val enrollmentPeriod: EnrollmentPeriod?
    )

    fun getObservable(member: Member): LiveData<ViewState> {
        setLiveDataSource(member)
        return viewStateObservable
    }

    @Synchronized
    internal fun setLiveDataSource(member: Member) {
        sourceLiveData?.let { viewStateObservable.removeSource(it) }
        sourceLiveData = createLiveDataFromLoadMember(member)
        sourceLiveData?.let { viewStateObservable.addSource(it, ViewStateObserver()) }
    }

    internal inner class ViewStateObserver : Observer<ViewState> {
        override fun onChanged(viewState: ViewState?) {
            viewState?.let { viewStateObservable.postValue(it) }
        }
    }

    internal fun createLiveDataFromLoadMember(member: Member): LiveData<ViewState> {
        val loadHouseholdFlowable = loadHouseholdUseCase.execute(member.householdId)
        val enrollmentPeriodFlowable = loadCurrentEnrollmentPeriodUseCase.executeFlowable()

        val flowable = Flowable.combineLatest(
            listOf(loadHouseholdFlowable, enrollmentPeriodFlowable),
            { results ->
                val householdWithMembersAndPayments = results[0] as HouseholdWithMembersAndPayments
                ViewState(
                    householdWithMembersAndPayments.members.find { it.member.id == member.id },
                    householdWithMembersAndPayments,
                    results[1] as EnrollmentPeriod
                )
            }
        ).onErrorReturn {
            logger.error(it)
            ViewState(null, null, null)
        }

        return LiveDataReactiveStreams.fromPublisher(flowable)
    }

    internal fun callIfMemberExists(updateMember: (member: Member) -> Completable): Completable {
        viewStateObservable.value?.memberWithThumbnail?.member?.let { member ->
            return updateMember(member)
        }
        return Completable.never()
    }

    fun updateName(name: String): Completable {
        return callIfMemberExists { member ->
            updateMemberUseCase.execute(member.copy(name = name))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun validateName(name: String?, errorString: String): String? {
        if (name == null || (!Member.isValidFullName(name, BuildConfig.MEMBER_FULL_NAME_MIN_LENGTH))) {
            return errorString
        }
        return null
    }

    fun updateGender(gender: Gender): Completable {
        return callIfMemberExists { member ->
            updateMemberUseCase.execute(member.copy(gender = gender))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun updateBirthdate(birthdate: LocalDate, birthdateAccuracy: DateAccuracy): Completable {
        return callIfMemberExists { member ->
            updateMemberUseCase.execute(
                member.copy(birthdate = birthdate, birthdateAccuracy = birthdateAccuracy)
            ).observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun updatePhoneNumber(phoneNumberString: String): Completable {
        return callIfMemberExists { member ->
            val phoneNumber = if (phoneNumberString.isBlank()) null else phoneNumberString
            updateMemberUseCase.execute(member.copy(phoneNumber = phoneNumber))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun updateMedicalRecordNumber(medicalRecordNumberString: String): Completable {
        return callIfMemberExists { member ->
            val medicalRecordNumber = if (medicalRecordNumberString.isBlank()) null else medicalRecordNumberString
            updateMemberUseCase.execute(member.copy(medicalRecordNumber = medicalRecordNumber))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun validateMedicalRecordNumber(medicalRecordNumberString: String?, errorString: String): String? {
        return if (medicalRecordNumberString == null ||
            Member.isValidMedicalRecordNumber(
                medicalRecordNumber = medicalRecordNumberString,
                minLength = BuildConfig.MEMBER_MEDICAL_RECORD_NUMBER_MIN_LENGTH,
                maxLength = BuildConfig.MEMBER_MEDICAL_RECORD_NUMBER_MAX_LENGTH
            )
        ) {
            null
        } else {
            errorString
        }
    }

    fun updatePhoto(rawPhotoId: UUID, thumbnailPhotoId: UUID): Completable {
        return callIfMemberExists { member ->
            updateMemberUseCase.execute(member.copy(
                    photoId = rawPhotoId, thumbnailPhotoId = thumbnailPhotoId))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun updateMemberCard(cardId: String): Completable {
        return callIfMemberExists { member ->
            updateMemberUseCase.execute(member.copy(cardId = cardId))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun updateProfession(profession: String): Completable {
        return callIfMemberExists { member ->
            updateMemberUseCase.execute(
                member.copy(profession = profession)
            ).observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun updateRelationshipToHead(relationshipToHead: String): Completable {
        return callIfMemberExists { member ->
            updateMemberUseCase.execute(
                member.copy(relationshipToHead = relationshipToHead)
            ).observeOn(AndroidSchedulers.mainThread())
        }
    }
}

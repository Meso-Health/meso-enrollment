package org.watsi.enrollment.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.enrollment.R
import org.watsi.enrollment.domain.entities.AdministrativeDivision
import org.watsi.enrollment.domain.entities.EnrollmentPeriod
import org.watsi.enrollment.domain.entities.Household
import org.watsi.enrollment.domain.usecases.LoadCurrentEnrollmentPeriodUseCase
import org.watsi.enrollment.domain.usecases.LoadAdministrativeDivisionsUseCase
import org.watsi.enrollment.flowstates.HouseholdFlowState
import java.lang.IllegalStateException
import java.util.UUID
import javax.inject.Inject

class NewHouseholdViewModel @Inject constructor(
    private val loadAdministrativeDivisionsUseCase: LoadAdministrativeDivisionsUseCase,
    private val loadCurrentEnrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase,
    private val clock: Clock
) : ViewModel() {

    private val viewStateObservable: MutableLiveData<ViewState> = MutableLiveData()

    companion object {
        const val MISSING_ADMIN_DIVISION_ERROR = "admin_division_error"
        const val MISSING_DATE_ERROR = "missing_date_error"
        const val INVALID_DATE_ERROR = "invalid_date_error"
    }

    fun getObservable(membershipNumber: String?): LiveData<ViewState> {
        viewStateObservable.value = ViewState(membershipNumber = membershipNumber)
        loadCurrentEnrollmentPeriodUseCase.executeSingle().subscribe { currentEnrollmentPeriod ->
            viewStateObservable.value?.let {
                viewStateObservable.postValue(it.copy(currentEnrollmentPeriod = currentEnrollmentPeriod))
            }
        }
        return viewStateObservable
    }

    fun getAdminDivisionChoicesObservable(level: String): LiveData<List<AdministrativeDivision>> {
        return LiveDataReactiveStreams.fromPublisher(loadAdministrativeDivisionsUseCase.execute(level))
    }

    fun onAdminDivisionChange(adminDivisionSelected: AdministrativeDivision?) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key == MISSING_ADMIN_DIVISION_ERROR }
            viewStateObservable.value = it.copy(adminDivisionSelected = adminDivisionSelected, errors = errors)
        }
    }

    fun onHouseNumberChange(houseNumber: String) {
        val nonEmptyHouseNumber = if (houseNumber.isBlank()) null else houseNumber
        viewStateObservable.value = viewStateObservable.value?.copy(houseNumber = nonEmptyHouseNumber)
    }

    fun updateEnrollmentDate(enrollmentDate: Instant) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key === MISSING_DATE_ERROR || it.key === INVALID_DATE_ERROR }
            viewStateObservable.value = it.copy(enrollmentDate = enrollmentDate, errors = errors)
        }
    }

    fun enrollmentDate(): Instant? {
        return viewStateObservable.value?.enrollmentDate
    }

    fun errors(): Map<String, Int>? {
        return viewStateObservable.value?.errors
    }

    fun validateFields(): Completable {
        val viewState = viewStateObservable.value

        if (viewState == null) {
            return Completable.never()
        } else {
            val validationErrors = FormValidator.formValidationErrors(viewState, clock)

            if (validationErrors.isNotEmpty()) {
                viewStateObservable.value = viewState.copy(errors = validationErrors)
                return Completable.error(Exception("Some fields are missing"))
            }
        }

        return Completable.complete()
    }

    fun toHouseholdFlowState(): HouseholdFlowState {
        viewStateObservable.value?.let { viewState ->
            if (viewState.adminDivisionSelected != null && viewState.enrollmentDate != null) {
                return HouseholdFlowState(
                    household = Household(
                        id = UUID.randomUUID(),
                        enrolledAt = viewState.enrollmentDate,
                        administrativeDivisionId = viewState.adminDivisionSelected.id,
                        address = viewState.houseNumber
                    ),
                    householdEnrollmentRecords = emptyList(),
                    members = mutableListOf(),
                    payments = mutableListOf(),
                    manualMembershipNumber = viewState.membershipNumber,
                    administrativeDivision = viewState.adminDivisionSelected
                )
            }
        }
        throw IllegalStateException("ViewStateToEntityMapper.toHouseholdFlowState should only be called with a valid viewState. " + viewStateObservable.toString())
    }

    object FormValidator {
        fun formValidationErrors(viewState: ViewState, clock: Clock): Map<String, Int> {
            val errors = HashMap<String, Int>()

            if (viewState.adminDivisionSelected == null) {
                errors[MISSING_ADMIN_DIVISION_ERROR] = R.string.household_admin_division_validation_error
            }

            if (viewState.enrollmentDate == null) {
                errors[MISSING_DATE_ERROR] = R.string.enrollment_date_missing_error
            } else {
                // Enforce that the enrollment date cannot be before the start of the enrollment period.
                viewState.currentEnrollmentPeriod?.let { currentEnrollmentPeriod ->
                    if (currentEnrollmentPeriod.startDate.atStartOfDay(clock.zone).toInstant().isAfter(viewState.enrollmentDate)) {
                        errors[INVALID_DATE_ERROR] = R.string.enrollment_date_validation_error
                    }
                }
            }
            return errors
        }
    }

    data class ViewState(
        val enrollmentDate: Instant? = Instant.now(),
        val adminDivisionSelected: AdministrativeDivision? = null,
        val houseNumber: String? = null,
        val membershipNumber: String? = null,
        val currentEnrollmentPeriod: EnrollmentPeriod? = null,
        val errors: Map<String, Int> = emptyMap()
    )
}

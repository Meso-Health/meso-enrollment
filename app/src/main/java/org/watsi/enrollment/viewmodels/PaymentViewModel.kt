package org.watsi.enrollment.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.enrollment.R
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.entities.EnrollmentPeriod
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.MembershipPayment
import org.watsi.enrollment.domain.entities.PaymentFees
import org.watsi.enrollment.domain.entities.PaymentType
import org.watsi.enrollment.domain.usecases.LoadCurrentEnrollmentPeriodUseCase
import org.watsi.enrollment.flowstates.HouseholdFlowState
import java.util.UUID
import javax.inject.Inject

class PaymentViewModel @Inject constructor(
    private val loadCurrentEnrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase,
    private val clock: Clock,
    private val logger: Logger
) : ViewModel() {

    private val viewStateObservable = MutableLiveData<ViewState>()
    lateinit var householdFlowState: HouseholdFlowState

    companion object {
        const val RECEIPT_NUMBER_ERROR = "receipt_number_error"
        const val PAYMENT_DATE_ERROR = "payment_date_error"
    }

    fun getObservable(householdFlowState: HouseholdFlowState, paymentType: PaymentType): LiveData<ViewState> {

        this.householdFlowState = householdFlowState

        viewStateObservable.value = ViewState(
            isPaying = householdFlowState.wasPayingMostRecentPeriod() ?: true,
            paymentType = paymentType
        )
        loadCurrentEnrollmentPeriodUseCase.executeSingle()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
            viewStateObservable.value = viewStateObservable.value?.copy(enrollmentPeriod = it)
            calculatePaymentFees()
        }, {
            logger.error(it)
        })
        return viewStateObservable
    }

    fun willChangeMembershipPaying(): Boolean {
        return householdFlowState.wasPayingMostRecentPeriod() != viewStateObservable.value?.isPaying
    }

    fun onBeneficiarySwitchToggled(memberId: UUID, isChecked: Boolean) {
        val toggledMembers = householdFlowState.members.map { member ->
            if (member.id == memberId) {
                if (isChecked) {
                    member.copy(archivedAt = null, archivedReason = null)
                } else {
                    member.copy(archivedAt = clock.instant(), archivedReason = Member.ARCHIVED_REASON_UNPAID)
                }
            } else {
                member
            }
        }
        householdFlowState.members = toggledMembers.toMutableList()
        calculatePaymentFees()
    }

    fun onIsPayingChange(isPaying: Boolean) {
        viewStateObservable.value = viewStateObservable.value?.copy(isPaying = isPaying)
        calculatePaymentFees()
    }

    fun onReceiptNumberChange(receiptNumber: String) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key === RECEIPT_NUMBER_ERROR }
            viewStateObservable.value = it.copy(receiptNumber = receiptNumber, errors = errors)
        }
    }

    fun updatePaymentDate(paymentDate: LocalDate) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key === PAYMENT_DATE_ERROR }
            viewStateObservable.value = it.copy(paymentDate = paymentDate, errors = errors)
        }
    }

    private fun calculatePaymentFees() {
        viewStateObservable.value?.let {
            it.enrollmentPeriod?.let { enrollmentPeriod ->
                val paymentFees = PaymentFees.calculate(
                    householdWithMembersAndPayments = householdFlowState.toHouseholdWithMembersAndPayment(),
                    isPaying = it.isPaying,
                    isReplacingCard = it.isReplacingCard,
                    isNewEnrollment = it.paymentType == PaymentType.NEW_ENROLLMENT,
                    penalty = it.penalty ?: 0,
                    other = it.other ?: 0,
                    enrollmentPeriod = enrollmentPeriod
                )

                viewStateObservable.value = it.copy(paymentFees = paymentFees)
            }
        }
    }

    fun onCardReplacementChange(isReplacing: Boolean) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(isReplacingCard = isReplacing)
        }
        calculatePaymentFees()
    }


    fun onPenaltyChange(penalty: Int?) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(penalty = penalty)
        }
        calculatePaymentFees()
    }

    fun onOtherChange(other: Int?) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(other = other)
        }
        calculatePaymentFees()
    }

    fun paymentDate(): LocalDate? {
        return viewStateObservable.value?.paymentDate
    }

    fun toMembershipPayment(householdEnrollmentRecordId: UUID): MembershipPayment? {
        return viewStateObservable.value?.let {
            val paymentNumbers = it.paymentFees
            if (it.receiptNumber == null || it.paymentDate == null || !it.isPaying ||
                    paymentNumbers == null) {
                return null
            }
            return MembershipPayment(
                id = UUID.randomUUID(),
                receiptNumber = it.receiptNumber,
                paymentDate = it.paymentDate,
                householdEnrollmentRecordId = householdEnrollmentRecordId,
                annualContributionFee = paymentNumbers.annualContributionFee,
                qualifyingBeneficiariesFee = paymentNumbers.beneficiariesFee,
                registrationFee = paymentNumbers.initialEnrollmentFee,
                cardReplacementFee = paymentNumbers.cardFee,
                penaltyFee = paymentNumbers.penaltyFee,
                otherFee = paymentNumbers.otherFee
            )
        }
    }

    fun isPaying(): Boolean? {
        return viewStateObservable.value?.isPaying
    }

    fun enrollmentPeriod(): EnrollmentPeriod? = viewStateObservable.value?.enrollmentPeriod

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

            if (viewState.isPaying) {
                if (viewState.receiptNumber.isNullOrEmpty()) {
                    errors[RECEIPT_NUMBER_ERROR] = R.string.receipt_number_validation_error
                }

                if (viewState.paymentDate == null) {
                    errors[PAYMENT_DATE_ERROR] = R.string.payment_date_validation_error
                }
            }

            return errors
        }
    }

    data class ViewState(
        val isPaying: Boolean = false,
        val paymentType: PaymentType,
        val enrollmentPeriod: EnrollmentPeriod? = null,
        val receiptNumber: String? = null,
        val paymentDate: LocalDate? = null,
        val isReplacingCard: Boolean = false,
        val penalty: Int? = null,
        val other: Int? = null,
        val paymentFees: PaymentFees? = null,
        val errors: Map<String, Int> = emptyMap()
    )
}

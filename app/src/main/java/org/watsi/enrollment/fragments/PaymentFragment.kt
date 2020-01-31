package org.watsi.enrollment.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ScrollView
import android.widget.TextView
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_payment.enrollment_date
import kotlinx.android.synthetic.main.fragment_payment.fee_summary
import kotlinx.android.synthetic.main.fragment_payment.indigent_button
import kotlinx.android.synthetic.main.fragment_payment.member_list
import kotlinx.android.synthetic.main.fragment_payment.membership_type_container
import kotlinx.android.synthetic.main.fragment_payment.next
import kotlinx.android.synthetic.main.fragment_payment.other_value
import kotlinx.android.synthetic.main.fragment_payment.paying_button
import kotlinx.android.synthetic.main.fragment_payment.payment_date
import kotlinx.android.synthetic.main.fragment_payment.payment_editable_fields_container
import kotlinx.android.synthetic.main.fragment_payment.penalty_value
import kotlinx.android.synthetic.main.fragment_payment.receipt_number
import kotlinx.android.synthetic.main.fragment_payment.receipt_number_container
import kotlinx.android.synthetic.main.fragment_payment.replace_card_checkbox
import kotlinx.android.synthetic.main.fragment_payment.replace_card_container
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.enrollment.R
import org.watsi.enrollment.activities.MainActivity
import org.watsi.enrollment.adapters.LineItemAdapter
import org.watsi.enrollment.adapters.MemberAdapter
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.entities.Household
import org.watsi.enrollment.domain.entities.PaymentFees
import org.watsi.enrollment.domain.entities.PaymentType
import org.watsi.enrollment.domain.entities.User
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import org.watsi.enrollment.domain.usecases.EnrollHouseholdUseCase
import org.watsi.enrollment.domain.usecases.LoadCurrentEnrollmentPeriodUseCase
import org.watsi.enrollment.domain.usecases.LoadUnpersistedMembersWithThumbnailsUseCase
import org.watsi.enrollment.domain.usecases.RenewHouseholdUseCase
import org.watsi.enrollment.domain.usecases.SaveMembershipPaymentUseCase
import org.watsi.enrollment.flowstates.HouseholdFlowState
import org.watsi.enrollment.helpers.EthiopianDateHelper
import org.watsi.enrollment.helpers.LayoutHelper
import org.watsi.enrollment.helpers.SnackbarHelper
import org.watsi.enrollment.helpers.StringHelper.getStringNullSafe
import org.watsi.enrollment.managers.NavigationManager
import org.watsi.enrollment.utils.CurrencyUtil
import org.watsi.enrollment.viewmodels.PaymentViewModel
import java.util.UUID
import javax.inject.Inject

class PaymentFragment : DaggerFragment(), NavigationManager.HandleOnBack {
    @Inject lateinit var renewHouseholdUseCase: RenewHouseholdUseCase
    @Inject lateinit var enrollHouseholdUseCase: EnrollHouseholdUseCase
    @Inject lateinit var saveMembershipPaymentUseCase: SaveMembershipPaymentUseCase
    @Inject lateinit var loadUnpersistedMembersWithThumbnailsUseCase: LoadUnpersistedMembersWithThumbnailsUseCase
    @Inject lateinit var loadEnrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var logger: Logger
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var clock: Clock
    @Inject lateinit var sessionManager: SessionManager

    lateinit var viewModel: PaymentViewModel
    lateinit var observable: LiveData<PaymentViewModel.ViewState>
    lateinit var householdFlowState: HouseholdFlowState
    lateinit var paymentType: PaymentType
    private val membersWithThumbnails = mutableListOf<MemberWithThumbnail>()

    companion object {
        const val PARAM_HOUSEHOLD_FLOWSTATE = "household_flowstate"
        const val PARAM_PAYMENT_TYPE = "payment_type"
        const val AUTO_SCROLL_PADDING_IN_DP = 60
        const val DISABLED_BUTTON_ALPHA = 0.3f
        const val ACTIVE_BUTTON_ALPHA = 1.0f

        fun forHousehold(
            householdFlowState: HouseholdFlowState,
            paymentType: PaymentType
        ): PaymentFragment {
            val reviewFragment = PaymentFragment()
            reviewFragment.arguments = Bundle().apply {
                putSerializable(PARAM_HOUSEHOLD_FLOWSTATE, householdFlowState)
                putString(PARAM_PAYMENT_TYPE, paymentType.toString())
            }
            return reviewFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        householdFlowState = arguments.get(PARAM_HOUSEHOLD_FLOWSTATE) as HouseholdFlowState
        paymentType = PaymentType.valueOf(arguments.getString(PARAM_PAYMENT_TYPE))

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PaymentViewModel::class.java)
        observable = viewModel.getObservable(householdFlowState, paymentType)

        observable.observe(this, Observer { viewState ->
            viewState?.let {
                setErrors(it.errors)

                if (it.isPaying) {
                    paying_button.setSelected()
                    payment_editable_fields_container.visibility = View.VISIBLE
                } else {
                    indigent_button.setSelected()
                    payment_editable_fields_container.visibility = View.GONE
                }

                it.paymentDate?.let { localDate ->
                    payment_date.setDate(localDate, clock)
                }

                it.paymentFees?.let { paymentFees ->
                    setPaymentLineItems(paymentFees)
                    if (it.isPaying && paymentFees.total() <= 0) {
                        next.isClickable = false
                        next.alpha = DISABLED_BUTTON_ALPHA
                    } else {
                        next.isClickable = true
                        next.alpha = ACTIVE_BUTTON_ALPHA
                    }
                }
            }
        })
    }

    private fun setPaymentLineItems(paymentFees: PaymentFees) {
        val lineItems = mutableListOf(
            LineItemAdapter.LineItem(R.string.contribution, paymentFees.annualContributionFee),
            LineItemAdapter.LineItem(R.string.registration_fee, paymentFees.initialEnrollmentFee),
            LineItemAdapter.LineItem(R.string.additional_household_member, paymentFees.beneficiariesFee, paymentFees.beneficiariesCount),
            LineItemAdapter.LineItem(R.string.card_replacement_fee_label, paymentFees.cardFee),
            LineItemAdapter.LineItem(R.string.penalty_fee_label, paymentFees.penaltyFee),
            LineItemAdapter.LineItem(R.string.other_fees, paymentFees.otherFee)
        )

        when (paymentType) {
            // don't show registration fee if a renewal or additional payment
            PaymentType.RENEWAL, PaymentType.MANUAL_RENEWAL, PaymentType.ADDITIONAL_PAYMENT -> {
                lineItems.removeIf { it.text == R.string.registration_fee }
            }
            // don't show card replacement fee if it is a new enrollment
            PaymentType.NEW_ENROLLMENT -> lineItems.removeIf { it.text == R.string.card_replacement_fee_label }
        }

        fee_summary.setLineItems(
            lineItems,
            moneyFormatter = { amount: Int -> CurrencyUtil.formatMoneyWithCurrency(context, amount) }
        )
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setToolbar(R.string.review_title, R.drawable.ic_clear_white_24dp)
        return inflater?.inflate(R.layout.fragment_payment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val numMembers = householdFlowState.members.size

        if (numMembers == 0) {
            throw IllegalStateException("The payment screen requires at least one member to be defined")
        }

        member_list.adapter = createMemberAdapter(isPaying = householdFlowState.wasPayingMostRecentPeriod() ?: true)
        member_list.layoutManager = LinearLayoutManager(activity)
        member_list.isNestedScrollingEnabled = false

        loadUnpersistedMembersWithThumbnailsUseCase.execute(householdFlowState.members).observeOn(AndroidSchedulers.mainThread()).subscribe( { personsWithThumbnails ->
            val sortedMembers = MemberWithThumbnail.asSortedListWithHeadOfHouseholdsFirst(personsWithThumbnails)
            this.membersWithThumbnails.clear()
            this.membersWithThumbnails.addAll(sortedMembers)
            member_list.adapter.notifyDataSetChanged()
        }, {
            logger.error(it)
        })

        // If this is payment outside the enrollment or renewal flow (e.g. paying for an additional member)
        // then the membership type cannot be changed. Set the button action to show an alert dialog instead
        val membershipTypeButtonAction: (Boolean) -> Unit = if (paymentType == PaymentType.ADDITIONAL_PAYMENT) {
            { isPaying ->
                if (isPaying) {
                    // The membership type will always be "Paying" in an "Additional payment" so we can do nothing
                    // if the paying button is pressed
                } else {
                    AlertDialog.Builder(context)
                        .setMessage(R.string.cannot_toggle_indigent_message)
                        .setPositiveButton(R.string.confirm) { _, _ -> }
                        .create()
                        .show()
                }
            }
        } else {
            { isPaying ->
                membership_type_container.requestFocus()
                onMembershipTypeChange(isPaying)
            }
        }

        paying_button.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                membershipTypeButtonAction(true)
                true
            } else {
                false
            }
        }
        indigent_button.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                membershipTypeButtonAction(false)
                true
            } else {
                false
            }
        }

        replace_card_container.visibility = if (paymentType == PaymentType.NEW_ENROLLMENT) View.GONE else View.VISIBLE

        replace_card_checkbox.setOnClickListener {
            viewModel.onCardReplacementChange((it as CheckBox).isChecked)
        }

        receipt_number.addTextChangedListener(LayoutHelper.OnChangedListener { text ->
            viewModel.onReceiptNumberChange(text)
        })

        penalty_value.addTextChangedListener(LayoutHelper.OnChangedListener { text ->
            viewModel.onPenaltyChange(CurrencyUtil.parseMoney(text))
        })

        other_value.addTextChangedListener(LayoutHelper.OnChangedListener { text ->
            viewModel.onOtherChange(CurrencyUtil.parseMoney(text))
        })

        // Below makes payment field not editable, only clickable to launch date dialog
        payment_date.setUp(
            initialGregorianValue = LocalDate.now(),
            clock = clock,
            onDateSelected = { paymentDateTime ->
                viewModel.updatePaymentDate(paymentDateTime.atZone(clock.zone).toLocalDate())
            }
        )

        when (paymentType) {
            PaymentType.NEW_ENROLLMENT -> next.setText(R.string.enroll_household_button)
            PaymentType.RENEWAL -> next.setText(R.string.renew_household_button)
            PaymentType.MANUAL_RENEWAL -> next.setText(R.string.renew_household_button)
            PaymentType.ADDITIONAL_PAYMENT -> next.setText(R.string.confirm_payment)
        }

        enrollment_date.text = EthiopianDateHelper.instantToFormattedEthiopianDate(
            householdFlowState.household.enrolledAt,
            clock
        )

        next.setOnClickListener {
            viewModel.validateFields().subscribe({
                val household = householdFlowState.household
                val user = sessionManager.currentAuthenticationToken()?.user!!

                when (paymentType) {
                    PaymentType.NEW_ENROLLMENT -> enrollHousehold(user, household)
                    PaymentType.RENEWAL -> renewHousehold(user, household)
                    // for manual renewals we need to save all records which is same behavior
                    // as enrollHousehold
                    PaymentType.MANUAL_RENEWAL -> renewHousehold(user, household)
                    PaymentType.ADDITIONAL_PAYMENT -> makeAdditionalPayment(household)
                }
            } , {
                showSnackbarForFieldErrors(it)
            })
        }
    }

    private fun enrollHousehold(user: User, household: Household) {
        enrollHouseholdUseCase.execute(
            household = household,
            paying = viewModel.isPaying()!!,
            renewal = paymentType == PaymentType.MANUAL_RENEWAL,
            members = householdFlowState.members,
            // assign an ID to the MembershipPayment because the HouseholdEnrollmentRecord is not
            // instantiated yet, but will use the ID from the MembershipPayment if available
            payment = viewModel.toMembershipPayment(UUID.randomUUID()),
            user = user
        ).subscribe({
            navigationManager.popTo(HomeFragment())
        }, { error ->
            logger.error(error)
            view?.let {
                SnackbarHelper.showError(it, context, error.localizedMessage)
            }
        })
    }

    private fun renewHousehold(user: User, household: Household) {
        renewHouseholdUseCase.execute(
            household = householdFlowState.toHouseholdWithMembersAndPayment(),
            paying = viewModel.isPaying()!!,
            payment = viewModel.toMembershipPayment(UUID.randomUUID()),
            user = user,
            clock = clock
        ).subscribe({
            navigationManager.popTo(HouseholdFragment.forHousehold(household.id))
        }, { error ->
            logger.error(error)
            view?.let {
                SnackbarHelper.showError(it, context, error.localizedMessage)
            }
        })
    }

    private fun makeAdditionalPayment(household: Household) {
        loadEnrollmentPeriodUseCase.executeSingle().subscribe { enrollmentPeriod ->
            householdFlowState.activeHouseholdEnrollmentRecord(enrollmentPeriod)?.let { householdEnrollmentRecord ->
                viewModel.toMembershipPayment(householdEnrollmentRecord.id)?.let {
                    saveMembershipPaymentUseCase.execute(it, householdFlowState.members).subscribe({
                        navigationManager.popTo(HouseholdFragment.forHousehold(household.id))
                    }, { error ->
                        logger.error(error)
                        view?.let {
                            SnackbarHelper.showError(it, context, error.localizedMessage)
                        }
                    })
                }
            }
        }
    }

    private fun onMembershipTypeChange(isPaying: Boolean) {
        if (isPaying) {
            paying_button.setSelected()
            indigent_button.setUnselected(R.drawable.ic_check_white_24dp)
            viewModel.onIsPayingChange(true)
        } else {
            paying_button.setUnselected(R.drawable.ic_check_white_24dp)
            indigent_button.setSelected()
            viewModel.onIsPayingChange(false)
        }
        member_list.adapter = createMemberAdapter(isPaying)
    }

    private fun createMemberAdapter(isPaying: Boolean): MemberAdapter {
        return if (isPaying) {
            MemberAdapter(
                members = membersWithThumbnails,
                clickHandler = null,
                toggleHandler = { memberId, isChecked -> viewModel.onBeneficiarySwitchToggled(memberId, isChecked) },
                showToggleFunction = { member ->
                            (paymentType != PaymentType.ADDITIONAL_PAYMENT // for renewals allow any member to be toggled
                            || (paymentType == PaymentType.ADDITIONAL_PAYMENT && member.unpaid())) // for additional payments only allow unpaid
                }
            )
        } else {
            MemberAdapter(members = membersWithThumbnails, clickHandler = null)
        }
    }

    private fun setErrors(errorMap: Map<String, Int?>) {
        receipt_number_container.error = getStringNullSafe(errorMap[PaymentViewModel.RECEIPT_NUMBER_ERROR], context)
        payment_date.setError(getStringNullSafe(errorMap[PaymentViewModel.PAYMENT_DATE_ERROR], context))
    }

    private fun scrollToFirstError(errorMap: Map<String, Int>) {
        val validationKeysToField = linkedMapOf(
            PaymentViewModel.RECEIPT_NUMBER_ERROR to receipt_number_container,
            PaymentViewModel.PAYMENT_DATE_ERROR to payment_date
        )

        validationKeysToField.forEach {
            val validationKey = it.key
            val layout = it.value
            if (errorMap[validationKey] != null) {
                (view as ScrollView).smoothScrollTo(0, layout.top - AUTO_SCROLL_PADDING_IN_DP)
                return
            }
        }
    }

    private fun showSnackbarForFieldErrors(throwable: Throwable) {
        viewModel.errors()?.let {
            val errorMessage = throwable.localizedMessage
            scrollToFirstError(it)

            view?.let {
                val snackbar = Snackbar.make(it, errorMessage, Snackbar.LENGTH_LONG)
                val textView = snackbar.view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
                textView.setTextColor(context.getColor(R.color.white))
                snackbar.view.setBackgroundColor(context.getColor(R.color.red6))
                snackbar.show()
            }
        }
    }

    override fun onBack(): Single<Boolean> {
        return Single.create<Boolean> { single ->
            AlertDialog.Builder(activity)
                    .setTitle(R.string.exit_form_alert)
                    .setMessage(R.string.new_household_dialog_message)
                    .setPositiveButton(R.string.ok) { _, _ -> single.onSuccess(true) }
                    .setNegativeButton(R.string.cancel) { _, _ -> single.onSuccess(false) }
                    .setOnDismissListener { single.onSuccess(false) }
                    .show()
        }
    }
}

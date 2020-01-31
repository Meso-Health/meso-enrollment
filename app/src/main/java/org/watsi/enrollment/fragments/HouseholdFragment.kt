package org.watsi.enrollment.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_household.add_new_beneficiary_button
import kotlinx.android.synthetic.main.fragment_household.banner
import kotlinx.android.synthetic.main.fragment_household.member_list
import kotlinx.android.synthetic.main.fragment_household.number_of_members_and_village
import kotlinx.android.synthetic.main.fragment_household.payment_button
import org.threeten.bp.Clock
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.R
import org.watsi.enrollment.activities.MainActivity
import org.watsi.enrollment.adapters.MemberAdapter
import org.watsi.enrollment.domain.entities.EnrollmentPeriod
import org.watsi.enrollment.domain.entities.PaymentType
import org.watsi.enrollment.domain.relations.HouseholdWithMembersAndPayments
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import org.watsi.enrollment.flowstates.HouseholdFlowState
import org.watsi.enrollment.managers.NavigationManager
import org.watsi.enrollment.viewmodels.HouseholdViewModel
import java.util.UUID
import javax.inject.Inject

class HouseholdFragment : DaggerFragment() {
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var clock: Clock

    lateinit var viewModel: HouseholdViewModel
    private lateinit var householdId: UUID
    private val members = mutableListOf<MemberWithThumbnail>()
    private var householdWithMembersAndPayments: HouseholdWithMembersAndPayments? = null
    private var activeEnrollmentPeriod: EnrollmentPeriod? = null

    companion object {
        const val PARAM_HOUSEHOLD_ID = "household_id"

        fun forHousehold(householdId: UUID): HouseholdFragment {
            val householdFragment = HouseholdFragment()
            householdFragment.arguments = Bundle().apply {
                putString(PARAM_HOUSEHOLD_ID, householdId.toString())
            }
            return householdFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        householdId = UUID.fromString(arguments.getString(PARAM_HOUSEHOLD_ID))
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HouseholdViewModel::class.java)
        setupObservable()
    }

    override fun onResume() {
        super.onResume()

        // this is required for when the user back navigates into this screen
        // the observable does not trigger, so we need to set the adapter from the viewModel
        setupObservable()
    }

    private fun setupObservable() {
        viewModel.getObservable(householdId).observe(this, Observer { viewState ->
            viewState?.let {
                householdWithMembersAndPayments = it.householdWithMembers
                activeEnrollmentPeriod = it.enrollmentPeriod
                if (it.householdWithMembers != null && it.enrollmentPeriod != null) {
                    updateView(it.householdWithMembers, it.enrollmentPeriod)
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setToolbar(R.string.household_title, 0)
        return inflater?.inflate(R.layout.fragment_household, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        member_list.adapter = MemberAdapter(
            members = members,
            clickHandler = { member -> navigationManager.goTo(EditMemberFragment.forMember(member.member)) }
        )

        member_list.layoutManager = LinearLayoutManager(activity)
        member_list.isNestedScrollingEnabled = false

        add_new_beneficiary_button.setOnClickListener {
            getHouseholdFlowState()?.let { householdFlowState ->
                navigationManager.goTo(NewMemberFragment.forHousehold(householdFlowState, members.size, false))
            }
        }

        val paymentRelatedButtons = listOf(payment_button, banner)
        paymentRelatedButtons.forEach { button ->
            button.setOnClickListener {
                getHouseholdFlowState()?.let { householdFlowState ->
                    activeEnrollmentPeriod?.let { enrollmentPeriod ->
                        navigationManager.goTo(PaymentFragment.forHousehold(
                            householdFlowState,
                            determinePaymentType(householdFlowState, enrollmentPeriod)
                        ))
                    }
                }
            }
        }
    }

    private fun determinePaymentType(
        householdFlowState: HouseholdFlowState,
        enrollmentPeriod: EnrollmentPeriod
    ): PaymentType {
        return if (householdFlowState.activeHouseholdEnrollmentRecord(enrollmentPeriod) == null) {
            PaymentType.RENEWAL
        } else {
            PaymentType.ADDITIONAL_PAYMENT
        }
    }

    private fun updateView(
        householdWithMembers: HouseholdWithMembersAndPayments,
        enrollmentPeriod: EnrollmentPeriod
    ) {
        val sortedMembers = MemberWithThumbnail.asSortedListWithHeadOfHouseholdsFirst(householdWithMembers.members)
        members.clear()
        members.addAll(sortedMembers)

        val membershipNumberString = members.mapNotNull { it.member.membershipNumber }.min()
        number_of_members_and_village.text =
                resources.getQuantityString(R.plurals.household_summary,
                    members.size,
                    members.size,
                    householdWithMembers.administrativeDivision?.name,
                    membershipNumberString ?: getString(R.string.blank_membership_number)
                )
        member_list.adapter.notifyDataSetChanged()

        // These buttons should only be visible if the setting to collect payment is turned on.
        if (BuildConfig.ENABLE_COLLECT_PAYMENT_INFO) {
            // show button unless it is an indigent who has already renewed
            if ((householdWithMembers.isPaying(enrollmentPeriod) == true) ||
                    householdWithMembers.needsRenewal(enrollmentPeriod)) {
                payment_button.visibility = View.VISIBLE
            }

            if (householdWithMembers.needsRenewal(enrollmentPeriod)) {
                banner.visibility = View.VISIBLE
                banner.setMessageAndAction(
                    getString(R.string.needs_to_renew_membership),
                    getString(R.string.renew)
                )
                payment_button.text = getString(R.string.renew)
            } else if (householdWithMembers.needsPayment(enrollmentPeriod)) {
                banner.visibility = View.VISIBLE
                banner.setMessageAndAction(
                    getString(R.string.beneficiary_requires_payment),
                    getString(R.string.pay)
                )
                payment_button.text = getString(R.string.additional_beneficiary_payment_button)
            } else {
                payment_button.text = getString(R.string.card_replacement_button)
            }
        }
    }

    private fun getHouseholdFlowState(): HouseholdFlowState? {
        return householdWithMembersAndPayments?.let {
            val members = it.members.map { it.member }.toMutableList()
            HouseholdFlowState(
                household = it.household,
                householdEnrollmentRecords = it.householdEnrollmentRecords,
                members = members,
                payments = it.payments.toMutableList(),
                administrativeDivision = it.administrativeDivision
            )
        }
    }
}

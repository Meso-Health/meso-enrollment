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
import kotlinx.android.synthetic.main.fragment_new_household.admin_division_spinner_field
import kotlinx.android.synthetic.main.fragment_stats.currency
import kotlinx.android.synthetic.main.fragment_stats.enrolled_beneficiaries_count
import kotlinx.android.synthetic.main.fragment_stats.enrolled_members_count
import kotlinx.android.synthetic.main.fragment_stats.gender_spinner_field
import kotlinx.android.synthetic.main.fragment_stats.paying_spinner_field
import kotlinx.android.synthetic.main.fragment_stats.payment_details
import kotlinx.android.synthetic.main.fragment_stats.renewal_spinner_field
import kotlinx.android.synthetic.main.fragment_stats.total_collected
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.R
import org.watsi.enrollment.R.string.female
import org.watsi.enrollment.R.string.male
import org.watsi.enrollment.R.string.new_enrollment_type
import org.watsi.enrollment.R.string.renewal_enrollment_type
import org.watsi.enrollment.activities.MainActivity
import org.watsi.enrollment.adapters.LineItemAdapter
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.utils.CurrencyUtil
import org.watsi.enrollment.viewmodels.StatsViewModel
import javax.inject.Inject

class StatsFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(StatsViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->
                enrolled_members_count.text = viewState.enrolledMembersCount.toString()
                enrolled_beneficiaries_count.text = viewState.enrolledBeneficiariesCount.toString()
                total_collected.text = CurrencyUtil.formatMoney(viewState.totalCollected)
                currency.text = BuildConfig.CURRENCY

                payment_details.adapter = LineItemAdapter(
                    viewState.paymentDetails,
                    { amount: Int -> CurrencyUtil.formatMoneyWithCurrency(context, amount) }
                )
            }
        })

        viewModel.getAdminDivisionChoicesObservable().observe(this, Observer {
            it?.let { adminDivisions ->
                val adminDivisionsByName = adminDivisions.sortedBy { it.name }
                admin_division_spinner_field.setUpWithPrompt(
                    choices = adminDivisionsByName.map { it.name },
                    initialChoice = null,
                    onItemSelected = { index ->
                        viewModel.onAdminDivisionChange(listOf(adminDivisionsByName[index]))
                    },
                    promptString = getString(R.string.not_filtered_value),
                    onPromptSelected = { viewModel.onAdminDivisionChange(adminDivisions) }
                )

                // use a simple list of booleans as filter for paying/indigent as paying is being
                // stored as a boolean value on the HouseholdEnrollmentRecord
                val payingList = listOf(true, false)
                paying_spinner_field.setUpWithPrompt(
                    choices = listOf(getString(R.string.paying), getString(R.string.indigent)),
                    initialChoice = null,
                    onItemSelected = { index ->
                        viewModel.onPayingChange(listOf(payingList[index]))
                    },
                    promptString = getString(R.string.not_filtered_value),
                    onPromptSelected = { viewModel.onPayingChange(payingList) }
                )

                val genderList = listOf(Gender.M, Gender.F)
                gender_spinner_field.setUpWithPrompt(
                    choices = listOf(getString(male), getString(female)),
                    initialChoice = null,
                    onItemSelected = { index ->
                        viewModel.onGenderChange(listOf(genderList[index]))
                    },
                    promptString = getString(R.string.not_filtered_value),
                    onPromptSelected = { viewModel.onGenderChange(genderList) }
                )

                val renewalList = listOf(false, true)
                renewal_spinner_field.setUpWithPrompt(
                    choices = listOf(getString(new_enrollment_type), getString(renewal_enrollment_type)),
                    initialChoice = null,
                    onItemSelected = { index ->
                        viewModel.onRenewalChange(listOf(renewalList[index]))
                    },
                    promptString = getString(R.string.not_filtered_value),
                    onPromptSelected = { viewModel.onRenewalChange(renewalList) }
                )
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setToolbar(R.string.menu_stats_label, 0)
        return inflater?.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        payment_details.layoutManager = LinearLayoutManager(context)
        payment_details.isNestedScrollingEnabled = false

        admin_division_spinner_field.setLabel(BuildConfig.HOUSEHOLD_ADMIN_DIVISION_LEVEL)
    }
}

package org.watsi.enrollment.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_new_household.admin_division_spinner_field
import kotlinx.android.synthetic.main.fragment_new_household.enrollment_date_container
import kotlinx.android.synthetic.main.fragment_new_household.house_number_field
import kotlinx.android.synthetic.main.fragment_new_household.next
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.R
import org.watsi.enrollment.activities.MainActivity
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.entities.EnrollmentPeriod
import org.watsi.enrollment.domain.utils.DateUtils
import org.watsi.enrollment.helpers.EthiopianDateHelper
import org.watsi.enrollment.helpers.LayoutHelper
import org.watsi.enrollment.helpers.SnackbarHelper
import org.watsi.enrollment.helpers.StringHelper.getStringNullSafe
import org.watsi.enrollment.managers.NavigationManager
import org.watsi.enrollment.viewmodels.NewHouseholdViewModel
import javax.inject.Inject

class NewHouseholdFragment : DaggerFragment(), NavigationManager.HandleOnBack {
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var logger: Logger
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var clock: Clock

    lateinit var viewModel: NewHouseholdViewModel
    lateinit var observable: LiveData<NewHouseholdViewModel.ViewState>
    lateinit var currentEnrollmentPeriod: EnrollmentPeriod

    companion object {
        const val PARAM_MEMBERSHIP_NUMBER = "membership_number"

        fun renewal(membershipNumber: String): NewHouseholdFragment {
            val newHouseholdFragment = NewHouseholdFragment()
            newHouseholdFragment.arguments = Bundle().apply {
                putString(PARAM_MEMBERSHIP_NUMBER, membershipNumber)
            }
            return newHouseholdFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(NewHouseholdViewModel::class.java)
        observable = if (arguments?.containsKey(PARAM_MEMBERSHIP_NUMBER) == true) {
            viewModel.getObservable(arguments.getString(PARAM_MEMBERSHIP_NUMBER))
        } else {
            viewModel.getObservable(null)
        }

        observable.observe(this, Observer {
            it?.let {
                setErrors(it.errors)

                it.enrollmentDate?.let { enrollmentDate ->
                    enrollment_date_container.setDate(enrollmentDate.atZone(clock.zone).toLocalDate(), clock)
                }

                it.currentEnrollmentPeriod?.let { currentEnrollmentPeriod ->
                    this.currentEnrollmentPeriod = currentEnrollmentPeriod
                }
            }
        })

        // split this out to its own observable becuase setting up the spinner calls onAdminDivisionChange
        // when initializing which leads to an infinite loop if inside the other observable callback
        viewModel.getAdminDivisionChoicesObservable(BuildConfig.HOUSEHOLD_ADMIN_DIVISION_LEVEL).observe(this, Observer {
            it?.let { adminDivisions ->
                val adminDivisionsByName = adminDivisions.sortedBy { it.name }
                admin_division_spinner_field.setUpWithPrompt(
                    choices = adminDivisionsByName.map { it.name },
                    initialChoice = null,
                    onItemSelected = { index ->
                        viewModel.onAdminDivisionChange(adminDivisionsByName[index])
                    },
                    promptString = getString(R.string.household_admin_division_spinner_prompt, BuildConfig.HOUSEHOLD_ADMIN_DIVISION_LEVEL),
                    onPromptSelected = { viewModel.onAdminDivisionChange(null) }
                )
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setToolbar(R.string.new_household_title, R.drawable.ic_clear_white_24dp)
        (activity as MainActivity).setSoftInputModeToPan()
        return inflater?.inflate(R.layout.fragment_new_household, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        enrollment_date_container.setUp(
            initialGregorianValue = LocalDate.now(),
            clock = clock,
            onDateSelected = { enrollmentDateTime ->
                viewModel.updateEnrollmentDate(enrollmentDateTime)
            }
        )

        if (BuildConfig.ENABLE_COLLECT_ADDRESS) {
            house_number_field.addTextChangedListener(LayoutHelper.OnChangedListener { text ->
                viewModel.onHouseNumberChange(text)
            })
            house_number_field.visibility = View.VISIBLE
        }

        next.setOnClickListener {
            viewModel.validateFields().subscribe({
                navigationManager.goTo(NewMemberFragment.forHousehold(viewModel.toHouseholdFlowState(), 0, true))
            }, {
                showSnackbarForFieldErrors(it)
            })
        }

        admin_division_spinner_field.setLabel(BuildConfig.HOUSEHOLD_ADMIN_DIVISION_LEVEL)
    }

    private fun setErrors(errorMap: Map<String, Int?>) {
        admin_division_spinner_field.setError(getStringNullSafe(errorMap[NewHouseholdViewModel.MISSING_ADMIN_DIVISION_ERROR], context))
        enrollment_date_container.setError(getStringNullSafe(errorMap[NewHouseholdViewModel.MISSING_DATE_ERROR], context))
        // Needed to set this here because I need to show the user the enrollment period start date, and
        // we can only format strings here since viewState returns the ID of the string resource (not the string itself)
        if (errorMap.containsKey(NewHouseholdViewModel.INVALID_DATE_ERROR)) {
            val formattedEnrollmentPeriodStartDate = if (BuildConfig.CALENDAR_LOCALE == "ethiopia") {
                EthiopianDateHelper.internationalDateToFormattedEthiopianDate(
                    currentEnrollmentPeriod.startDate, clock
                )
            } else {
                DateUtils.formatLocalDate(currentEnrollmentPeriod.startDate)
            }
            enrollment_date_container.setError(getString(R.string.enrollment_date_validation_error, formattedEnrollmentPeriodStartDate))
        }
    }

    private fun showSnackbarForFieldErrors(throwable: Throwable) {
        viewModel.errors()?.let {
            SnackbarHelper.showError(next, context, throwable.localizedMessage)
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

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).setSoftInputModeToResize()
    }
}

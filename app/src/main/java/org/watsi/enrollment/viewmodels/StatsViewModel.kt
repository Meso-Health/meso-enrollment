package org.watsi.enrollment.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.watsi.enrollment.R
import org.watsi.enrollment.adapters.LineItemAdapter
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.entities.AdministrativeDivision
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.usecases.LoadAdministrativeDivisionsUseCase
import org.watsi.enrollment.domain.usecases.LoadSummaryStatisticsUseCase
import org.watsi.enrollment.domain.usecases.LoadSummaryStatisticsUseCase.StatsFilter
import javax.inject.Inject

class StatsViewModel @Inject constructor (
    private val loadSummaryStatisticsUseCase: LoadSummaryStatisticsUseCase,
    private val loadAdministrativeDivisionsUseCase: LoadAdministrativeDivisionsUseCase,
    private val logger: Logger
) : ViewModel() {

    private val viewStateObservable = MutableLiveData<StatsViewModel.ViewState>()
    private val statsFilter = StatsFilter(emptyList(), emptyList(), emptyList(), emptyList())

    fun getObservable(): LiveData<ViewState> {
        viewStateObservable.value = ViewState(
            enrolledMembersCount = 0,
            enrolledBeneficiariesCount = 0,
            totalCollected = 0,
            paymentDetails = emptyList(),
            error = null
        )

        return viewStateObservable
    }

    fun onAdminDivisionChange(adminDivisionFilter: List<AdministrativeDivision>) {
        statsFilter.adminDivisionFilter = adminDivisionFilter
        updateStats()
    }

    fun onPayingChange(payingFilter: List<Boolean>) {
        statsFilter.payingFilter = payingFilter
        updateStats()
    }

    fun onGenderChange(genderFilter: List<Gender>) {
        statsFilter.genderFilter = genderFilter
        updateStats()
    }

    fun onRenewalChange(renewalFilter: List<Boolean>) {
        statsFilter.renewalFilter = renewalFilter
        updateStats()
    }

    private fun updateStats() {
        viewStateObservable.value?.let {
            loadSummaryStatisticsUseCase.execute(statsFilter).subscribe({ statistics ->
                val paymentFees = statistics.third
                val paymentDetails = listOf(
                    LineItemAdapter.LineItem(R.string.contribution, paymentFees.annualContributionFee),
                    LineItemAdapter.LineItem(R.string.registration_fee, paymentFees.initialEnrollmentFee),
                    LineItemAdapter.LineItem(R.string.additional_household_member, paymentFees.beneficiariesFee),
                    LineItemAdapter.LineItem(R.string.card_replacement_fee_label, paymentFees.cardFee),
                    LineItemAdapter.LineItem(R.string.penalty_fee_label, paymentFees.penaltyFee),
                    LineItemAdapter.LineItem(R.string.other_fees, paymentFees.otherFee)
                )

                viewStateObservable.postValue(it.copy(
                    enrolledMembersCount = statistics.first,
                    enrolledBeneficiariesCount = statistics.second,
                    totalCollected = paymentFees.total(),
                    paymentDetails = paymentDetails,
                    error = null
                ))
            }, {
                viewStateObservable.postValue(errorState(it))
            })
        }
    }

    fun getAdminDivisionChoicesObservable(): LiveData<List<AdministrativeDivision>> {
        return LiveDataReactiveStreams.fromPublisher(loadAdministrativeDivisionsUseCase.execute(level = "gote"))
    }

    private fun errorState(error: Throwable): ViewState {
        logger.error(error)
        return ViewState(
            enrolledMembersCount = 0,
            enrolledBeneficiariesCount = 0,
            totalCollected = 0,
            paymentDetails = emptyList(),
            error = error.localizedMessage
        )
    }

    data class ViewState(
        val enrolledMembersCount: Int,
        val enrolledBeneficiariesCount: Int,
        val totalCollected: Int,
        val paymentDetails: List<LineItemAdapter.LineItem>,
        val error: String?
    )
}

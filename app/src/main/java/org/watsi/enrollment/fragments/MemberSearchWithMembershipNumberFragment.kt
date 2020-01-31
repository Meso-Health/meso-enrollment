/*
    This screen allows searching by membership number, QR code, or name.
 */
package org.watsi.enrollment.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_search_with_membership_number.membership_number
import kotlinx.android.synthetic.main.fragment_search_with_membership_number.membership_number_button
import kotlinx.android.synthetic.main.fragment_search_with_membership_number.scan_card_button
import kotlinx.android.synthetic.main.fragment_search_with_membership_number.search_button
import kotlinx.android.synthetic.main.fragment_search_with_membership_number.search_by_name_button
import org.watsi.enrollment.R
import org.watsi.enrollment.activities.MainActivity
import org.watsi.enrollment.activities.QrCodeActivity
import org.watsi.enrollment.activities.SearchHouseholdByCardActivity
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.usecases.FindHouseholdIdByMembershipNumberUseCase
import org.watsi.enrollment.helpers.SnackbarHelper
import org.watsi.enrollment.managers.NavigationManager
import java.util.UUID
import javax.inject.Inject

class MemberSearchWithMembershipNumberFragment : DaggerFragment() {
    @Inject lateinit var findHouseholdByMembershipNumberUseCase: FindHouseholdIdByMembershipNumberUseCase
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var logger: Logger

    companion object {
        const val SEARCH_HOUSEHOLD_BY_CARD_INTENT = 1
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setToolbar(R.string.search_title, 0)
        return inflater?.inflate(R.layout.fragment_search_with_membership_number, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        search_button.setOnClickListener {
            val membershipNumber = membership_number.text.toString()
            // Only execute use case if membership number is not blank.
            if (!membershipNumber.isBlank()) {
                findHouseholdByMembershipNumberUseCase.execute(membershipNumber).subscribe({ householdId ->
                    navigationManager.goTo(HouseholdFragment.forHousehold(householdId))
                }, { err ->
                    logger.error(err)
                    view?.let {
                        SnackbarHelper.showError(it, context, err.localizedMessage)
                    }
                }, {
                    navigationManager.goTo(MemberNotFoundFragment.forMembershipNumber(membershipNumber))
                })
            }
        }

        membership_number_button.setTextColor(context.getColor(R.color.blue4))
        membership_number_button.compoundDrawableTintList = context.getColorStateList(R.color.blue4)

        scan_card_button.setOnClickListener {
            startActivityForResult(Intent(activity, SearchHouseholdByCardActivity::class.java),
                SEARCH_HOUSEHOLD_BY_CARD_INTENT)
        }

        search_by_name_button.setOnClickListener {
            navigationManager.goTo(MemberSearchFragment())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SEARCH_HOUSEHOLD_BY_CARD_INTENT) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val householdId = it.getSerializableExtra(SearchHouseholdByCardActivity.HOUSEHOLD_ID_KEY) as UUID
                        navigationManager.goTo(HouseholdFragment.forHousehold(householdId))
                    }
                }

                Activity.RESULT_CANCELED -> { } // Do nothing, but don't trigger an error

                QrCodeActivity.RESULT_NOT_FOUND -> {
                    data?.let { _ ->
                        view?.let {
                            SnackbarHelper.showError(
                                it, context, getString(R.string.card_not_found_error_message))
                        }
                    }
                }

                QrCodeActivity.RESULT_BARCODE_DETECTOR_NOT_OPERATIONAL -> {
                    logger.error("Barcode detector not operational")
                }
                else -> {
                    logger.error("Unknown result code ($resultCode) from SearchHouseholdByCardActivity")
                }
            }
        }
    }
}

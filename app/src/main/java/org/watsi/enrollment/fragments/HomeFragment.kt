package org.watsi.enrollment.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_home.edit_button
import kotlinx.android.synthetic.main.fragment_home.enroll_button
import kotlinx.android.synthetic.main.fragment_home.recent_button
import kotlinx.android.synthetic.main.fragment_home.renew_button
import kotlinx.android.synthetic.main.fragment_home.reporting_button
import kotlinx.android.synthetic.main.fragment_home.status_button
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.R
import org.watsi.enrollment.activities.MainActivity
import org.watsi.enrollment.managers.NavigationManager
import javax.inject.Inject

class HomeFragment : DaggerFragment() {
    @Inject lateinit var navigationManager: NavigationManager

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setToolbar(R.string.enrollment, null)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enroll_button.setOnClickListener {
            navigationManager.goTo(NewHouseholdFragment())
        }

        listOf(renew_button, edit_button).forEach {
            it.setOnClickListener {
                if (BuildConfig.ENABLE_MEMBERSHIP_NUMBER_SEARCH) {
                    navigationManager.goTo(MemberSearchWithMembershipNumberFragment())
                } else {
                    navigationManager.goTo(MemberSearchFragment())
                }
            }
        }

        recent_button.setOnClickListener {
            navigationManager.goTo(RecentEditsFragment())
        }

        reporting_button.setOnClickListener {
            navigationManager.goTo(StatsFragment())
        }

        status_button.setOnClickListener {
            navigationManager.goTo(StatusFragment())
        }

        if (BuildConfig.ENABLE_RENEWALS) {
            renew_button.visibility = View.VISIBLE
        }

        if (BuildConfig.ENABLE_REPORTING) {
            reporting_button.visibility = View.VISIBLE
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.let {
            if (BuildConfig.ENABLE_LANGUAGE_SWITCH) {
                it.findItem(R.id.menu_switch_language).isVisible = true
            }
            it.findItem(R.id.menu_logout).isVisible = true
        }
    }
}

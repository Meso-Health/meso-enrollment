package org.watsi.enrollment.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_recent_edits.empty_image
import kotlinx.android.synthetic.main.fragment_recent_edits.household_list
import kotlinx.android.synthetic.main.fragment_recent_edits.household_list_container
import kotlinx.android.synthetic.main.fragment_recent_edits.search_household
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.R
import org.watsi.enrollment.activities.MainActivity
import org.watsi.enrollment.adapters.HouseholdAdapter
import org.watsi.enrollment.domain.relations.HouseholdWithMembers
import org.watsi.enrollment.managers.NavigationManager
import org.watsi.enrollment.viewmodels.RecentEditsViewModel
import javax.inject.Inject

class RecentEditsFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var navigationManager: NavigationManager
    private val allHouseholds = mutableListOf<HouseholdWithMembers>()
    private val householdsToShow = mutableListOf<HouseholdWithMembers>()
    private var currentPage = 0

    companion object {
        const val INFINITE_SCROLL_SLACK_IN_DP = 25
        const val INITIAL_HOUSEHOLD_COUNT_TO_LOAD = 10
        const val HOUSEHOLDS_PER_PAGE = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(RecentEditsViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->

                val updatedHouseholds = viewState.households
                allHouseholds.clear()
                allHouseholds.addAll(updatedHouseholds)
                householdsToShow.clear()
                householdsToShow.addAll(updatedHouseholds.take(INITIAL_HOUSEHOLD_COUNT_TO_LOAD))

                household_list.adapter.notifyDataSetChanged()
                if (allHouseholds.isEmpty()) {
                    empty_image.visibility = View.VISIBLE
                    household_list_container.visibility = View.GONE
                } else {
                    empty_image.visibility = View.GONE
                    household_list_container.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setToolbar(R.string.members_enrolled_label, 0)
        return inflater?.inflate(R.layout.fragment_recent_edits, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        household_list.adapter = HouseholdAdapter(householdsToShow, { householdWithMembers ->
            navigationManager.goTo(HouseholdFragment.forHousehold(householdWithMembers.household.id))
        })

        val layoutManager = LinearLayoutManager(activity)
        household_list.layoutManager = layoutManager
        household_list.isNestedScrollingEnabled = false
        val listItemDivider = DividerItemDecoration(context, layoutManager.orientation)
        listItemDivider.setDrawable(resources.getDrawable(R.drawable.household_list_divider, null))
        household_list.addItemDecoration(listItemDivider)

        household_list_container.setOnScrollChangeListener { _: View?, _: Int, scrollY: Int, _: Int, _: Int ->
            if (INITIAL_HOUSEHOLD_COUNT_TO_LOAD < allHouseholds.size && household_list.bottom <= scrollY + household_list_container.bottom + INFINITE_SCROLL_SLACK_IN_DP) {
                currentPage += 1
                householdsToShow.clear()
                householdsToShow.addAll(allHouseholds.take(INITIAL_HOUSEHOLD_COUNT_TO_LOAD + currentPage * HOUSEHOLDS_PER_PAGE))
                household_list.adapter.notifyDataSetChanged()
            }
        }

        search_household.setOnClickListener {
            if (BuildConfig.ENABLE_MEMBERSHIP_NUMBER_SEARCH) {
                navigationManager.goTo(MemberSearchWithMembershipNumberFragment())
            } else {
                navigationManager.goTo(MemberSearchFragment())
            }
        }
    }
}

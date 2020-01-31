/*
    This screen allows searching by QR code or name.
 */

package org.watsi.enrollment.fragments

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_member_search.empty_member_search_results
import kotlinx.android.synthetic.main.fragment_member_search.loading_indicator
import kotlinx.android.synthetic.main.fragment_member_search.member_search_results
import kotlinx.android.synthetic.main.fragment_member_search.qr_code_search_button
import org.threeten.bp.Clock
import org.watsi.enrollment.R
import org.watsi.enrollment.activities.MainActivity
import org.watsi.enrollment.activities.SearchHouseholdByCardActivity
import org.watsi.enrollment.adapters.MemberAdapter
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import org.watsi.enrollment.helpers.RecyclerViewHelper
import org.watsi.enrollment.managers.KeyboardManager
import org.watsi.enrollment.managers.NavigationManager
import org.watsi.enrollment.viewmodels.MemberSearchViewModel
import java.util.UUID
import javax.inject.Inject

class MemberSearchFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var clock: Clock

    lateinit var viewModel: MemberSearchViewModel
    lateinit var memberAdapter: MemberAdapter
    private val searchResults = mutableListOf<MemberWithThumbnail>()

    companion object {
        const val SEARCH_HOUSEHOLD_BY_CARD_INTENT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        memberAdapter = MemberAdapter(
            members = searchResults,
            clickHandler = { memberRelation: MemberWithThumbnail ->
                navigationManager.goTo(HouseholdFragment.forHousehold(memberRelation.member.householdId))
            }
        )

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MemberSearchViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->
                searchResults.clear()
                searchResults.addAll(viewState.matchingMembers)
                member_search_results.adapter.notifyDataSetChanged()

                if (viewState.loading) {
                    member_search_results.visibility = View.GONE
                    loading_indicator.visibility = View.VISIBLE
                    empty_member_search_results.visibility = View.GONE
                } else {
                    // If search has finished and there are no matching members...
                    if (viewState.matchingMembers.isEmpty() && viewState.searchMethod != null) {
                        member_search_results.visibility = View.GONE
                        empty_member_search_results.visibility = View.VISIBLE
                    } else {
                        // If search is finished and there are matching members...
                        member_search_results.visibility = View.VISIBLE
                        empty_member_search_results.visibility = View.GONE
                    }

                    // Regardless of results, we should hide the loading indicator.
                    loading_indicator.visibility = View.GONE
                }
            }
        })

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setToolbar(R.string.search_title, null)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_member_search, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        RecyclerViewHelper.setRecyclerView(member_search_results, memberAdapter, context, false)

        qr_code_search_button.setOnClickListener {
            startActivityForResult(Intent(activity, SearchHouseholdByCardActivity::class.java), SEARCH_HOUSEHOLD_BY_CARD_INTENT)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.member_search, menu)

        // auto-expand the SearchView in the Toolbar
        val searchItem = menu.findItem(R.id.search_member_name)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // need to include clearFocus() or else back button will only unfocus SearchView
                searchView.clearFocus()
                viewModel.updateQuery(query.toLowerCase())
                return false
            }
        })

        // auto-focus the SearchView
        searchView.isIconified = false
        searchView.requestFocus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        return when (resultCode) {
            Activity.RESULT_OK -> {
                val householdId = data?.getSerializableExtra(SearchHouseholdByCardActivity.HOUSEHOLD_ID_KEY) as UUID?
                if (householdId != null) {
                    navigationManager.goTo(HouseholdFragment.forHousehold(householdId))
                } else {
                    logger.error("QRCodeActivity returned null member with resultCode: Activity.RESULT_OK")
                }
            }
            Activity.RESULT_CANCELED -> { }
            else -> {
                logger.error("QrCodeActivity.parseResult called with resultCode: $resultCode")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).resetToolbarMinimal()
    }
}

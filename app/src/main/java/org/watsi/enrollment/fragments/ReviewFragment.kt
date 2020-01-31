package org.watsi.enrollment.fragments

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_review.enrollment_date
import kotlinx.android.synthetic.main.fragment_review.member_list
import kotlinx.android.synthetic.main.fragment_review.next
import org.threeten.bp.Clock
import org.watsi.enrollment.R
import org.watsi.enrollment.activities.MainActivity
import org.watsi.enrollment.adapters.MemberAdapter
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.entities.Household
import org.watsi.enrollment.domain.entities.User
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import org.watsi.enrollment.domain.usecases.EnrollHouseholdUseCase
import org.watsi.enrollment.domain.usecases.LoadUnpersistedMembersWithThumbnailsUseCase
import org.watsi.enrollment.flowstates.HouseholdFlowState
import org.watsi.enrollment.managers.NavigationManager
import javax.inject.Inject

class ReviewFragment : DaggerFragment(), NavigationManager.HandleOnBack {
    @Inject lateinit var enrollHouseholdUseCase: EnrollHouseholdUseCase
    @Inject lateinit var loadUnpersistedMembersWithThumbnailsUseCase: LoadUnpersistedMembersWithThumbnailsUseCase
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var logger: Logger
    @Inject lateinit var clock: Clock
    @Inject lateinit var sessionManager: SessionManager

    lateinit var householdFlowState: HouseholdFlowState
    private val membersWithThumbnails = mutableListOf<MemberWithThumbnail>()

    companion object {
        const val PARAM_HOUSEHOLD_FLOWSTATE = "household_flowstate"

        fun forHousehold(householdFlowState: HouseholdFlowState): ReviewFragment {
            val reviewFragment = ReviewFragment()
            reviewFragment.arguments = Bundle().apply {
                putSerializable(PARAM_HOUSEHOLD_FLOWSTATE, householdFlowState)
            }
            return reviewFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        householdFlowState = arguments.get(PARAM_HOUSEHOLD_FLOWSTATE) as HouseholdFlowState
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setToolbar(R.string.review_title, R.drawable.ic_clear_white_24dp)
        return inflater?.inflate(R.layout.fragment_review, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val numMembers = householdFlowState.members.size

        if (numMembers == 0) {
            throw IllegalStateException("The review screen requires at least one member to be defined")
        }

        member_list.adapter = MemberAdapter(members = membersWithThumbnails, clickHandler = null)
        member_list.layoutManager = LinearLayoutManager(activity)
        member_list.isNestedScrollingEnabled = false

        loadUnpersistedMembersWithThumbnailsUseCase.execute(householdFlowState.members).observeOn(AndroidSchedulers.mainThread()).subscribe({ personsWithThumbnails ->
            val sortedMembers = MemberWithThumbnail.asSortedListWithHeadOfHouseholdsFirst(personsWithThumbnails)
            this.membersWithThumbnails.clear()
            this.membersWithThumbnails.addAll(sortedMembers)
            member_list.adapter.notifyDataSetChanged()
        }, {
            logger.error(it)
        })

        enrollment_date.text = householdFlowState.household.enrolledAt.atZone(clock.zone).toLocalDate().toString()

        next.setText(R.string.enroll_household_button)
        next.setOnClickListener {
            val household = householdFlowState.household
            val user = sessionManager.currentAuthenticationToken()?.user!!
            enrollHousehold(user, household)
        }
    }

    private fun enrollHousehold(user: User, household: Household) {
        enrollHouseholdUseCase.execute(
            household = household,
            paying = false,
            renewal = false,
            members = householdFlowState.members,
            payment = null,
            user = user
        ).subscribe({
            // After enrolling someone, it should take them to the household fragment.
            // We need to go in order of the screens in order to not mess up the backstack.
            navigationManager.popTo(HomeFragment())
            navigationManager.goTo(RecentEditsFragment())
            navigationManager.goTo(HouseholdFragment.forHousehold(household.id))
        }, { error ->
            logger.error(error)
        })
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

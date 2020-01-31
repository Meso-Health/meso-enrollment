package org.watsi.enrollment.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_member_not_found.renew_button
import org.watsi.enrollment.R
import org.watsi.enrollment.activities.MainActivity
import org.watsi.enrollment.managers.NavigationManager
import javax.inject.Inject

class MemberNotFoundFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager

    companion object {
        const val PARAM_MEMBERSHIP_NUMBER = "membership_number"

        fun forMembershipNumber(membershipNumber: String): MemberNotFoundFragment {
            val memberNotFoundFragment = MemberNotFoundFragment()
            memberNotFoundFragment.arguments = Bundle().apply {
                putString(PARAM_MEMBERSHIP_NUMBER, membershipNumber)
            }
            return memberNotFoundFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setToolbar(R.string.member_not_found_fragment_label, 0)
        return inflater?.inflate(R.layout.fragment_member_not_found, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        renew_button.setOnClickListener {
            val membershipNumber = arguments.getString(PARAM_MEMBERSHIP_NUMBER)
            navigationManager.popTo(HomeFragment())
            navigationManager.goTo(NewHouseholdFragment.renewal(membershipNumber))
        }
    }
}

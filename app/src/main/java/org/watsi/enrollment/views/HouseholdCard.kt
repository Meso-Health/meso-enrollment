package org.watsi.enrollment.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.view_household_card.view.border1
import kotlinx.android.synthetic.main.view_household_card.view.member1
import kotlinx.android.synthetic.main.view_household_card.view.other_members
import kotlinx.android.synthetic.main.view_household_card.view.summary
import org.watsi.enrollment.R
import org.watsi.enrollment.domain.relations.HouseholdWithMembers
import org.watsi.enrollment.domain.relations.MemberWithThumbnail

class HouseholdCard @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    fun setHousehold(householdWithMembers: HouseholdWithMembers) {
        val membersWithPhotos = MemberWithThumbnail.asSortedListWithHeadOfHouseholdsFirst(householdWithMembers.members)
        val memberCount = membersWithPhotos.size
        val beneficiaryCount = memberCount - 1

        // min() will choose the first membership number string (when sorted alphabetically),
        // which hopefully yields the first enrolled member.
        val membershipNumberString = membersWithPhotos.mapNotNull { it.member.membershipNumber }.min()
        summary.text = resources.getString(
            R.string.household_card_summary_with_membership_number,
            householdWithMembers.administrativeDivision?.name ?: "",
            membershipNumberString ?: resources.getString(R.string.blank_membership_number)
        )

        listOf(border1, other_members).forEach {
            it.visibility = View.GONE
        }

        membersWithPhotos.getOrNull(0)?.let { memberWithPhoto ->
            member1.setMember(memberWithPhoto)
        }

        if (memberCount > 1) {
            border1.visibility = View.VISIBLE
            other_members.visibility = View.VISIBLE
            other_members.text = resources.getQuantityString(
                    R.plurals.household_card_more_members_notice, beneficiaryCount, beneficiaryCount)
        }
    }
}

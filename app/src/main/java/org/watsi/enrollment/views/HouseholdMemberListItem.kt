package org.watsi.enrollment.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.view_household_member_list_item.view.gender_age
import kotlinx.android.synthetic.main.view_household_member_list_item.view.member_icon
import kotlinx.android.synthetic.main.view_household_member_list_item.view.member_toggle
import kotlinx.android.synthetic.main.view_household_member_list_item.view.member_toggle_wrap
import kotlinx.android.synthetic.main.view_household_member_list_item.view.name
import kotlinx.android.synthetic.main.view_household_member_list_item.view.photo_container
import kotlinx.android.synthetic.main.view_household_member_list_item.view.unpaid_icon_container
import org.watsi.enrollment.R
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import org.watsi.enrollment.helpers.PhotoLoader
import org.watsi.enrollment.helpers.StringHelper
import java.util.UUID

class HouseholdMemberListItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val placeholderPhotoIconPadding = resources.getDimensionPixelSize(R.dimen.householdPhotoPlaceholderPadding)

    fun setParams(
        memberWithThumbnail: MemberWithThumbnail,
        toggleHandler: ((memberId: UUID, isChecked: Boolean) -> Unit)? = null
    ) {
        val member = memberWithThumbnail.member

        name.text = member.name
        val genderString = if (member.gender == Gender.F) {
            resources.getString(R.string.female)
        } else {
            resources.getString(R.string.male)
        }
        gender_age.text = resources.getString(R.string.member_list_item_gender_age,
                                              genderString,
                                              StringHelper.getDisplayAge(member, context))

        if (member.isHeadOfHousehold()) {
            member_icon.visibility = View.VISIBLE
        }

        if (member.unpaid()) {
            unpaid_icon_container.visibility = View.VISIBLE
        } else {
            unpaid_icon_container.visibility = View.GONE
        }

        toggleHandler?.let { handler ->
            member_toggle_wrap.visibility = View.VISIBLE

            // we want the toggles to be on by default
            member_toggle.isChecked = true
            handler(member.id, true)

            member_toggle.setOnCheckedChangeListener { _, isChecked ->
                handler(member.id, isChecked)
            }
        }

        PhotoLoader.loadMemberPhoto(
            bytes = memberWithThumbnail.photo?.bytes,
            view = photo_container,
            context = context,
            gender = member.gender,
            photoExists = member.photoExists(),
            placeholderPadding = placeholderPhotoIconPadding
        )
    }
}

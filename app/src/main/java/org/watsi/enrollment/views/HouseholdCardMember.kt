package org.watsi.enrollment.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.view_household_card_member.view.name
import kotlinx.android.synthetic.main.view_household_card_member.view.photo_container
import org.watsi.enrollment.R
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import org.watsi.enrollment.helpers.BitmapHelper
import org.watsi.enrollment.helpers.PhotoLoader

class HouseholdCardMember @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val placeholderPhotoIconPadding = resources.getDimensionPixelSize(R.dimen.memberPhotoPlaceholderPadding)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_household_card_member, this, true)
    }

    fun setMember(memberWithThumbnail: MemberWithThumbnail) {
        val member = memberWithThumbnail.member
        val photo = memberWithThumbnail.photo

        PhotoLoader.loadMemberPhoto(
            bytes = photo?.bytes,
            view = photo_container,
            context = context,
            gender = member.gender,
            photoExists = member.photoExists(),
            placeholderPadding = placeholderPhotoIconPadding
        )

        name.text = member.name
    }
}

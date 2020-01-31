package org.watsi.enrollment.helpers

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import org.watsi.enrollment.R
import org.watsi.enrollment.domain.entities.Gender

/**
 * This class contains photo helper methods that would help with loading images smoothly using Glide.
 */
object PhotoLoader {

    const val ROUNDING_RADIUS = 8
    val requestOptions = RequestOptions().transforms(CenterCrop(), RoundedCorners(ROUNDING_RADIUS))

    fun loadPhoto(bytes: ByteArray?, view: ImageView, context: Context) {
        Glide.with(context)
                .load(bytes)
                .apply(requestOptions)
                .into(view)
    }

    fun loadMemberPhoto(bytes: ByteArray?, view: ImageView, context: Context, gender: Gender, photoExists: Boolean, placeholderPadding: Int = 0) {
        val placeholder = if (gender == Gender.F) {
            if (photoExists) {
                R.drawable.ic_member_unsynced_placeholder_female
            } else {
                R.drawable.ic_member_placeholder_female
            }
        } else {
            if (photoExists) {
                R.drawable.ic_member_unsynced_placeholder_male
            } else {
                R.drawable.ic_member_placeholder_male
            }

        }

        if (bytes == null) {
            view.setPadding(placeholderPadding, placeholderPadding, placeholderPadding, placeholderPadding)
        } else {
            view.setPadding(0, 0, 0, 0)
        }

        requestOptions.fallback(placeholder)

        Glide.with(context)
            .load(bytes)
            .apply(requestOptions)
            .into(view)
    }
}

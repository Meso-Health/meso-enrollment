package org.watsi.enrollment.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.watsi.enrollment.R
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import org.watsi.enrollment.views.HouseholdMemberListItem
import java.util.UUID

class MemberAdapter(
    private val members: List<MemberWithThumbnail>,
    private val clickHandler: ((member: MemberWithThumbnail) -> Unit)? = null,
    private val toggleHandler: ((memberId: UUID, isChecked: Boolean) -> Unit)? = null,
    private val showToggleFunction: ((member: Member) -> Boolean)? = null
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    override fun getItemCount(): Int = members.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val memberView = LayoutInflater.from(parent.context).inflate(
            R.layout.view_household_member_list_item, parent, false)
        return MemberViewHolder(memberView)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val memberWithThumbnail = members[position]
        val householdMemberListItemView = holder.itemView as HouseholdMemberListItem
        householdMemberListItemView.setParams(
            memberWithThumbnail = memberWithThumbnail,
            toggleHandler = if (showToggleFunction != null && showToggleFunction.invoke(memberWithThumbnail.member)) toggleHandler else null
        )
        clickHandler?.let { handler ->
            householdMemberListItemView.setOnClickListener {
                handler(memberWithThumbnail)
            }
        }
    }

    class MemberViewHolder(memberView: View) : RecyclerView.ViewHolder(memberView)
}

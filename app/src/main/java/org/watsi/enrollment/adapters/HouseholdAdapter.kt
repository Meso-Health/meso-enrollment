package org.watsi.enrollment.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.watsi.enrollment.R
import org.watsi.enrollment.domain.relations.HouseholdWithMembers
import org.watsi.enrollment.views.HouseholdCard

class HouseholdAdapter(
    private val households: List<HouseholdWithMembers>,
    private val listener: (HouseholdWithMembers) -> Unit
) : RecyclerView.Adapter<HouseholdAdapter.HouseholdViewHolder>() {

    override fun getItemCount() = households.size

    override fun onBindViewHolder(holder: HouseholdViewHolder, position: Int) {
        val household = households[position]
        holder.bind(household, listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseholdViewHolder {
        val householdView = LayoutInflater.from(parent.context).inflate(
                R.layout.view_household_card, parent, false)
        return HouseholdViewHolder(householdView)
    }

    class HouseholdViewHolder(private val householdView: View) :
            RecyclerView.ViewHolder(householdView) {
        fun bind(household: HouseholdWithMembers, listener: (HouseholdWithMembers) -> Unit) {
            val householdCard = householdView as HouseholdCard

            householdCard.setHousehold(household)
            householdCard.setOnClickListener { listener(household) }
        }
    }
}

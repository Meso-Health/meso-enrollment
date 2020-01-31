package org.watsi.enrollment.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.watsi.enrollment.R
import org.watsi.enrollment.views.FeesLineItemView

class LineItemAdapter(
    private val items: List<LineItem>,
    private val moneyFormatter: ((Int) -> String)
) : RecyclerView.Adapter<LineItemAdapter.LineItemViewHolder>() {

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: LineItemViewHolder, position: Int) {
        val lineItem = items[position]
        val lineItemListView = holder.itemView as FeesLineItemView
        lineItemListView.setLineItem(lineItem, moneyFormatter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineItemViewHolder {
        val lineItemView = LayoutInflater.from(parent.context).inflate(
            R.layout.view_fees_summary_line_item, parent, false)
        return LineItemViewHolder(lineItemView)
    }

    class LineItemViewHolder(lineItemView: View) : RecyclerView.ViewHolder(lineItemView)

    data class LineItem(val text: Int, val totalPrice: Int, val quantityToShow: Int? = null)
}

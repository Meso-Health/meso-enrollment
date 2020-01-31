package org.watsi.enrollment.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.view_fees_summary.view.line_items
import kotlinx.android.synthetic.main.view_fees_summary.view.total_price
import org.watsi.enrollment.R
import org.watsi.enrollment.adapters.LineItemAdapter

class FeesSummaryView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    init {
        LayoutInflater.from(context).inflate(R.layout.view_fees_summary, this, true)
    }

    fun setLineItems(items: List<LineItemAdapter.LineItem>, moneyFormatter: ((Int) -> String)) {
        line_items.adapter = LineItemAdapter(items, moneyFormatter)
        line_items.layoutManager = LinearLayoutManager(context)
        line_items.isNestedScrollingEnabled = false
        total_price.text = moneyFormatter(items.sumBy { it.totalPrice })
    }
}

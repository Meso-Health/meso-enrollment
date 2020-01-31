package org.watsi.enrollment.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.view_fees_summary_line_item.view.guideline
import kotlinx.android.synthetic.main.view_fees_summary_line_item.view.label
import kotlinx.android.synthetic.main.view_fees_summary_line_item.view.price
import kotlinx.android.synthetic.main.view_fees_summary_line_item.view.quantity
import org.watsi.enrollment.adapters.LineItemAdapter

class FeesLineItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    fun setLineItem(lineItem: LineItemAdapter.LineItem, moneyFormatter: ((Int) -> String)) {
        if (lineItem.quantityToShow != null) {
            quantity.visibility = View.VISIBLE
            quantity.text = lineItem.quantityToShow.toString()
        } else {
            // This sets the guideline to be 0 pixels from the left so that the text can start without a spacing to the left.
            val layoutParams = guideline.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.guideBegin = 0
            guideline.layoutParams = layoutParams
        }

        label.text = context.getString(lineItem.text)
        price.text = moneyFormatter(lineItem.totalPrice)
    }
}

package org.watsi.enrollment.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.notification_bar.view.notification_btn

import org.watsi.enrollment.R

class NotificationBar(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val notificationMessage: TextView
    private val notificationBtn: Button

    init {
        LayoutInflater.from(context).inflate(R.layout.notification_bar, this)

        notificationMessage = findViewById(R.id.notification_message)
        notificationBtn = findViewById(R.id.notification_btn)

        val ta = getContext().obtainStyledAttributes(attrs, R.styleable.NotificationBar)

        try {
            val message = ta.getString(R.styleable.NotificationBar_message)
            val action = ta.getString(R.styleable.NotificationBar_action)

            if (message != null && action != null) {
                setMessageAndAction(message, action)
            }
        } finally {
            ta.recycle()
        }
    }

    fun setMessageAndAction(message: String, action: String) {
        notificationMessage.text = message
        notificationBtn.text = action
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        notification_btn.setOnClickListener(listener)
    }
}

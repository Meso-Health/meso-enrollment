package org.watsi.enrollment.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import kotlinx.android.synthetic.main.view_dialog_edit_date_field.view.date_value
import kotlinx.android.synthetic.main.view_dialog_edit_date_field.view.date_value_container
import kotlinx.android.synthetic.main.view_dialog_edit_date_field.view.field_label
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.R
import org.watsi.enrollment.domain.utils.DateUtils
import org.watsi.enrollment.helpers.EthiopianDateHelper


class DialogDateEditField @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    lateinit var selectedDate: LocalDate
    lateinit var clock: Clock
    lateinit var onDateSelected: ((gregorianInstant: Instant) -> Unit)

    companion object {
        const val DATE_PICKER_START_YEAR = 2008
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_dialog_edit_date_field, this, true)
        val customAttributes = context.obtainStyledAttributes(attrs, R.styleable.DialogDateEditField)
        field_label.text = customAttributes.getString(R.styleable.DialogDateEditField_label)
        customAttributes.recycle()
    }

    fun setDate(gregorianDate: LocalDate, clock: Clock) {
        this.selectedDate = gregorianDate
        if (BuildConfig.CALENDAR_LOCALE == "ethiopia") {
            val dateString = EthiopianDateHelper.internationalDateToFormattedEthiopianDate(gregorianDate, clock)
            date_value.setText(dateString)
        } else {
            date_value.setText(DateUtils.formatLocalDate(gregorianDate))
        }
    }

    fun setError(errorMessage: String?) {
        date_value_container.error = errorMessage
    }

    fun setUp(
        initialGregorianValue: LocalDate,
        clock: Clock,
        onDateSelected: ((gregorianInstant: Instant) -> Unit)
    ) {
        this.selectedDate = initialGregorianValue
        this.clock = clock
        this.onDateSelected = onDateSelected
        if (BuildConfig.CALENDAR_LOCALE == "ethiopia") {
            setUpEthiopiaCalendar()
        } else {
            setUpInternationalCalendar()
        }
    }

    private fun setUpInternationalCalendar() {
        setDate(this.selectedDate, clock)

        date_value.isFocusable = false
        date_value.inputType = 0
        // Set up the onclick listener
        date_value.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_international_datepicker, null)
            val datePicker = dialogView.findViewById<View>(R.id.date_picker) as DatePicker
            val timePicker = dialogView.findViewById<View>(R.id.time_picker) as TimePicker

            datePicker.maxDate = clock.instant().toEpochMilli()
            datePicker.updateDate(
                selectedDate.year,
                selectedDate.monthValue - 1, // LocalDate month values are 1 indexed while DatePicker months are 0 indexed.
                selectedDate.dayOfMonth
            )

            val builder = android.app.AlertDialog.Builder(context)
            builder.setView(dialogView)

            val dialog = builder.create()

            dialogView.findViewById<View>(R.id.done).setOnClickListener {
                val selectedDateTime = LocalDateTime.of(
                    datePicker.year,
                    datePicker.month + 1, // DatePicker months are zero indexed but LocalDateTime's are not
                    datePicker.dayOfMonth,
                    timePicker.hour,
                    timePicker.minute
                )
                onDateSelected(selectedDateTime.atZone(clock.zone).toInstant())
                dialog.dismiss()
            }

            dialogView.findViewById<View>(R.id.cancel).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun setUpEthiopiaCalendar() {
        setDate(this.selectedDate, clock)
        // Here we specify what kind of datepicker this will be, and try to move as much of the Ethiopia stuff away from this as possible.

        // Set up the dialog
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_ethiopian_date_picker, null)
        val daySpinner = dialogView.findViewById<View>(R.id.day_spinner) as SpinnerField
        val monthSpinner = dialogView.findViewById<View>(R.id.month_spinner) as SpinnerField
        val yearSpinner = dialogView.findViewById<View>(R.id.year_spinner) as SpinnerField

        val initialEthiopianValue = EthiopianDateHelper.internationalDateToEthiopianDate(this.selectedDate, clock)
        val todayDate = EthiopianDateHelper.internationalDateToEthiopianDate(LocalDate.now(clock.zone), clock)

        val dayAdapter = SpinnerField.createAdapter(
            context, (1..initialEthiopianValue.day).map { it.toString() })
        val monthAdapter = SpinnerField.createAdapter(
            context, (1..initialEthiopianValue.month).map { it.toString() })
        val yearAdapter = SpinnerField.createAdapter(
            context, (DATE_PICKER_START_YEAR..todayDate.year).map { it.toString() })

        daySpinner.setUpWithoutPrompt(
            adapter = dayAdapter,
            initialChoiceIndex = initialEthiopianValue.day - 1,
            onItemSelected = { /* No-op */ }
        )
        monthSpinner.setUpWithoutPrompt(
            adapter = monthAdapter,
            initialChoiceIndex = initialEthiopianValue.month - 1,
            onItemSelected = { monthString ->
                val daysToShow = EthiopianDateHelper.daysInMonthNotInFuture(
                    yearSpinner.getSelectedItem().toInt(), monthString.toInt(), todayDate)

                dayAdapter.clear()
                dayAdapter.addAll((1..daysToShow).map { it.toString() })
            }
        )
        yearSpinner.setUpWithoutPrompt(
            adapter = yearAdapter,
            initialChoiceIndex = initialEthiopianValue.year - DATE_PICKER_START_YEAR,
            onItemSelected = { yearString ->
                // Save the currently selected month in case the list shrinks
                var selectedMonth = monthSpinner.getSelectedItem().toInt()

                val monthsToShow = EthiopianDateHelper.monthsInYearNotInFuture(yearString.toInt(), todayDate)
                monthAdapter.clear()
                monthAdapter.addAll((1..monthsToShow).map { it.toString() })

                // The following code makes sure our selectedMonth is not larger than the list of months.
                // The Android spinner will do this automatically for us: if we reduce the adapter
                // to a smaller list than the selected index, it will automatically select the highest index
                // in the list. However, this has not happened yet, so we need to calculate this ourselves
                // to calculate the appropriate daysToShow value.
                if (selectedMonth > monthsToShow) selectedMonth = monthsToShow

                val daysToShow = EthiopianDateHelper.daysInMonthNotInFuture(
                    yearString.toInt(), selectedMonth, todayDate)
                dayAdapter.clear()
                dayAdapter.addAll((1..daysToShow).map { it.toString() })
            }
        )

        val builder = AlertDialog.Builder(context)
        builder.setView(dialogView)
        builder.setPositiveButton(R.string.dialog_save) { _, _ ->
            val ethiopianDate = EthiopianDateHelper.EthiopianDate(
                yearSpinner.getSelectedItem().toInt(),
                monthSpinner.getSelectedItem().toInt(),
                daySpinner.getSelectedItem().toInt()
            )
            val gregorianDate = EthiopianDateHelper.ethiopianDateToInternationalDate(ethiopianDate, clock)

            setDate(gregorianDate, clock)
            onDateSelected(gregorianDate.atStartOfDay(clock.zone).toInstant())
        }
        builder.setNegativeButton(R.string.dialog_cancel) { _, _ -> /* No-Op */ }

        val dateDialog = builder.create()

        date_value.isFocusable = false
        date_value.inputType = 0
        // Set up the onclick listener
        date_value.setOnClickListener {
            daySpinner.setSelectedItem(initialEthiopianValue.day - 1)
            monthSpinner.setSelectedItem(initialEthiopianValue.month - 1)
            yearSpinner.setSelectedItem(initialEthiopianValue.year - DATE_PICKER_START_YEAR)

            dateDialog.show()
        }
    }
}

package org.watsi.enrollment.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import kotlinx.android.synthetic.main.view_dialog_edit_field.view.action
import kotlinx.android.synthetic.main.view_dialog_edit_field.view.border
import kotlinx.android.synthetic.main.view_dialog_edit_field.view.field_error_message
import kotlinx.android.synthetic.main.view_dialog_edit_field.view.field_label
import kotlinx.android.synthetic.main.view_dialog_edit_field.view.input_icon
import kotlinx.android.synthetic.main.view_dialog_edit_field.view.input_value
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.R
import org.watsi.enrollment.domain.entities.DateAccuracy
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.utils.Age
import org.watsi.enrollment.domain.utils.DateUtils
import org.watsi.enrollment.helpers.EthiopianDateHelper
import org.watsi.enrollment.helpers.LayoutHelper
import org.watsi.enrollment.helpers.StringHelper
import org.watsi.enrollment.managers.KeyboardManager

/**
 * View for displaying a birthdate field that opens a custom Dialog to select either an age or
 * birthday value. Similar to DialogEditField in that it visually mimics the Material Design text
 * input and exposes a similar API for setting an onClick handler and onUpdate callback for
 * the Dialog.
 */
class DialogBirthdateField @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var birthdate: LocalDate? = null
    private var accuracy: DateAccuracy? = null
    private var isEthiopianCalendar: Boolean = false
    private lateinit var keyboardManager: KeyboardManager
    private lateinit var dialog: AlertDialog
    private lateinit var dialogLayout: View
    private lateinit var handleNewValue: (birthdate: LocalDate, accuracy: DateAccuracy, dialog: AlertDialog) -> Unit
    private lateinit var clock: Clock

    init {
        LayoutInflater.from(context).inflate(R.layout.view_dialog_edit_field, this, true)
        val customAttributes = context.obtainStyledAttributes(attrs, R.styleable.DialogBirthdateField)
        val showCalendarIcon = customAttributes.getBoolean(R.styleable.DialogBirthdateField_showCalendarIcon, false)
        isEthiopianCalendar = BuildConfig.CALENDAR_LOCALE == "ethiopia"

        field_label.visibility = View.INVISIBLE
        input_value.setText(R.string.birthdate_field_label)
        input_value.setTextColor(context.getColor(R.color.gray6))
        if (showCalendarIcon) {
            action.visibility = View.GONE
            input_icon.setImageResource(R.drawable.ic_date_range_black_24dp)
            input_icon.imageTintList = context.getColorStateList(R.color.gray6)
            input_icon.visibility = View.VISIBLE
        } else {
            action.setText(R.string.edit_input_edit)
        }

        customAttributes.recycle()
    }

    /**
     *  This method does three things:
     *  - Sets the error message
     *  - Underlines the field red
     *  - Makes the calendar icon (if exists) red.
     *
     *  This does NOT modify the dialog itself.
     */
    fun setErrorOnField(errorMessage: String?) {
        if (errorMessage != null) {
            field_error_message.error = errorMessage
            border.setBackgroundColor(context.getColor(R.color.red6))
            input_icon.imageTintList = context.getColorStateList(R.color.red6)
        } else {
            field_error_message.error = null
            border.setBackgroundColor(context.getColor(R.color.gray4))
            input_icon.imageTintList = context.getColorStateList(R.color.gray6)
        }
    }

    /**
     * Re-renders the internationalBirthdate field with the updated value.
     */
    fun setValue(internationalBirthdate: LocalDate, accuracy: DateAccuracy) {
        this.birthdate = internationalBirthdate
        this.accuracy = accuracy

        if (accuracy == DateAccuracy.D) {
            field_label.text = context.getString(R.string.birthdate_field_label)
        } else {
            field_label.text = context.getString(R.string.age_field_label)
        }

        if (isEthiopianCalendar && accuracy == DateAccuracy.D) {
            input_value.text = EthiopianDateHelper.internationalDateToFormattedEthiopianDate(internationalBirthdate, clock)
        } else {
            input_value.text = StringHelper.formatBirthdate(internationalBirthdate, accuracy, context)
        }

        field_label.visibility = View.VISIBLE
        input_value.setTextColor(context.getColor(R.color.gray9))
    }

    /**
     * Configures the birthdate field to launch an AlertDialog when clicked.
     * The AlertDialog allows selecting/changing the birthdate field and calls the
     * handleNewValue callback when done.
     */
    fun configureBirthdateDialog(keyboardManager: KeyboardManager,
                                 handleNewValue: (birthdate: LocalDate, accuracy: DateAccuracy, dialog: AlertDialog) -> Unit,
                                 clock: Clock) {
        this.keyboardManager = keyboardManager
        this.handleNewValue = handleNewValue
        this.clock = clock
        setOnClickListener {
            this.requestFocus()
            launchBirthdateDialog()
        }
    }

    private fun launchBirthdateDialog() {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogLayout = LayoutInflater.from(context).inflate(R.layout.dialog_select_birthdate, null)

        dialogBuilder.setView(dialogLayout)
        dialogBuilder.setPositiveButton(R.string.modal_save, { _, _ ->
            // no-op, handled in onClickListener below
        })
        dialogBuilder.setNegativeButton(R.string.modal_cancel, { dialogInterface, _ ->
            (dialogInterface as AlertDialog).dismiss()
        })
        dialog = dialogBuilder.create()
        dialog.show()

        val initialLaunch = accuracy == null
        if (accuracy == DateAccuracy.D || initialLaunch) { toggleDateInput() } else { toggleAgeInput() }
    }

    private fun toggleAgeInput() {
        val birthdateDialogTitle = dialogLayout.findViewById<TextView>(R.id.birthdate_dialog_title)
        val birthdateDialogToggle = dialogLayout.findViewById<TextView>(R.id.birthdate_dialog_toggle_input)
        val ageFields = dialogLayout.findViewById<ConstraintLayout>(R.id.age_fields)
        val dateFields = dialogLayout.findViewById<ConstraintLayout>(R.id.date_fields)
        val ageInputLayout = dialogLayout.findViewById<TextInputLayout>(R.id.age_input_layout)
        val ageInput = dialogLayout.findViewById<TextInputEditText>(R.id.age_input)
        val ageUnitSpinner = dialogLayout.findViewById<Spinner>(R.id.age_unit_spinner)
        val ageUnitAdapter = ArrayAdapter.createFromResource(
                context, R.array.age_units, android.R.layout.simple_spinner_dropdown_item)
        val birthdate = birthdate
        val accuracy = accuracy

        birthdateDialogTitle.setText(R.string.age_dialog_title)
        birthdateDialogToggle.setText(R.string.toggle_date_input)
        birthdateDialogToggle.setOnClickListener { toggleDateInput() }

        ageUnitSpinner.adapter = ageUnitAdapter
        if (birthdate != null && accuracy != null) {
            DateUtils.dateWithAccuracyToAge(birthdate, accuracy)?.let {
                ageInput.setText(it.quantity.toString())
                ageUnitSpinner.setSelection(ageUnitAdapter.getPosition(it.unit.toString()))
            }
        }
        ageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitAge(ageInput.text.toString(), ageUnitSpinner.selectedItem.toString())
                true
            } else {
                false
            }
        }
        ageInput.addTextChangedListener(LayoutHelper.OnChangedListener {
            _ -> ageInputLayout.error = null
        })

        ageFields.visibility = View.VISIBLE
        dateFields.visibility = View.GONE

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            submitAge(ageInput.text.toString(), ageUnitSpinner.selectedItem.toString())
        }
        ageInput.post { keyboardManager.showKeyboard(ageInput) }
    }

    private fun toggleDateInput() {
        val birthdateDialogTitle = dialogLayout.findViewById<TextView>(R.id.birthdate_dialog_title)
        val birthdateDialogToggle = dialogLayout.findViewById<TextView>(R.id.birthdate_dialog_toggle_input)
        val ageFields = dialogLayout.findViewById<ConstraintLayout>(R.id.age_fields)
        val dateFields = dialogLayout.findViewById<ConstraintLayout>(R.id.date_fields)
        val dayInput = dialogLayout.findViewById<TextInputEditText>(R.id.day_input)
        val monthInput = dialogLayout.findViewById<TextInputEditText>(R.id.month_input)
        val yearInput = dialogLayout.findViewById<TextInputEditText>(R.id.year_input)
        val calculatedAge = dialogLayout.findViewById<TextView>(R.id.calculated_age)
        val errorMessage = dialogLayout.findViewById<TextView>(R.id.date_error_message)
        val birthdate = birthdate
        val accuracy = accuracy

        birthdateDialogTitle.setText(R.string.birthdate_dialog_title)
        birthdateDialogToggle.setText(R.string.toggle_age_input)
        birthdateDialogToggle.setOnClickListener { toggleAgeInput() }

        if (birthdate != null && accuracy == DateAccuracy.D) {
            if (isEthiopianCalendar) {
                val ethBirthdate = EthiopianDateHelper.internationalDateToEthiopianDate(birthdate, clock)
                dayInput.setText(ethBirthdate.day.toString())
                monthInput.setText(ethBirthdate.month.toString())
                yearInput.setText(ethBirthdate.year.toString())
            } else {
                dayInput.setText(birthdate.dayOfMonth.toString())
                monthInput.setText(birthdate.monthValue.toString())
                yearInput.setText(birthdate.year.toString())
            }
            calculatedAge.text = formatCalculatedAge(birthdate)
            calculatedAge.visibility = View.VISIBLE
        }
        yearInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitDate(dayInput.text.toString(), monthInput.text.toString(), yearInput.text.toString())
                true
            } else {
                false
            }
        }
        dayInput.addTextChangedListener(LayoutHelper.OnChangedListener { text ->
            errorMessage.visibility = View.INVISIBLE
            updateCalculatedAge(text, monthInput.text.toString(), yearInput.text.toString(), calculatedAge)
        })
        monthInput.addTextChangedListener(LayoutHelper.OnChangedListener { text ->
            errorMessage.visibility = View.INVISIBLE
            updateCalculatedAge(dayInput.text.toString(), text, yearInput.text.toString(), calculatedAge)
        })
        yearInput.addTextChangedListener(LayoutHelper.OnChangedListener { text ->
            errorMessage.visibility = View.INVISIBLE
            updateCalculatedAge(dayInput.text.toString(), monthInput.text.toString(), text, calculatedAge)
        })

        ageFields.visibility = View.GONE
        dateFields.visibility = View.VISIBLE

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            submitDate(dayInput.text.toString(), monthInput.text.toString(), yearInput.text.toString())
        }
        dayInput.post { keyboardManager.showKeyboard(dayInput) }
    }

    private fun updateCalculatedAge(dayString: String, monthString: String, yearString: String, calculatedAgeView : TextView) {
        val day = dayString.toIntOrNull()
        val month = monthString.toIntOrNull()
        val year = yearString.toIntOrNull()

        if (day != null && month != null && year != null) {
            if (isEthiopianCalendar && EthiopianDateHelper.isValidEthiopianBirthdate(year, month, day, clock)){
                val internationalBirthdate = EthiopianDateHelper.ethiopianDateToInternationalDate(
                    EthiopianDateHelper.EthiopianDate(year, month, day), clock)
                calculatedAgeView.text = formatCalculatedAge(internationalBirthdate)
                calculatedAgeView.visibility = View.VISIBLE
                return
            }

            if (!isEthiopianCalendar && DateUtils.isValidBirthdate(year, month, day)) {
                val birthdate = LocalDate.of(year, month, day)
                calculatedAgeView.text = formatCalculatedAge(birthdate)
                calculatedAgeView.visibility = View.VISIBLE
                return
            }
        }

        calculatedAgeView.visibility = View.INVISIBLE
    }

    private fun formatCalculatedAge(localDate: LocalDate): String {
        val ageYears = DateUtils.getYearsAgo(localDate, clock)
        return if (ageYears >= 2) "$ageYears years old" else "${DateUtils.getMonthsAgo(localDate, clock)} months old"
    }

    private fun submitAge(ageQuantityString: String, ageUnitString: String) {
        val ageQuantity = ageQuantityString.toIntOrNull()
        val ageUnit = StringHelper.fromStringToAgeUnit(ageUnitString, context)

        when {
            ageQuantity == null -> {
                val ageInputLayout = dialogLayout.findViewById<TextInputLayout>(R.id.age_input_layout)
                ageInputLayout.error = context.getString(R.string.age_missing_error)
            }
            ageQuantity > Member.MAX_AGE -> {
                val ageInputLayout = dialogLayout.findViewById<TextInputLayout>(R.id.age_input_layout)
                ageInputLayout.error = context.getString(R.string.age_validation_error)
            }
            else -> {
                val age = Age(ageQuantity, ageUnit)
                val birthdateWithAccuracy = age.toBirthdateWithAccuracy()
                handleNewValue(birthdateWithAccuracy.first, birthdateWithAccuracy.second, dialog)
            }
        }
    }

    private fun submitDate(dayString: String, monthString: String, yearString: String) {
        val dayInput = dialogLayout.findViewById<TextInputEditText>(R.id.day_input)
        val monthInput = dialogLayout.findViewById<TextInputEditText>(R.id.month_input)
        val yearInput = dialogLayout.findViewById<TextInputEditText>(R.id.year_input)
        val errorMessage = dialogLayout.findViewById<TextView>(R.id.date_error_message)
        val day = dayString.toIntOrNull()
        val month = monthString.toIntOrNull()
        val year = yearString.toIntOrNull()

        errorMessage.visibility = View.INVISIBLE

        if (day == null) {
            errorMessage.text = context.getString(R.string.day_validation_error)
            errorMessage.visibility = View.VISIBLE
            dayInput.post { keyboardManager.showKeyboard(dayInput) }
        } else if (month == null) {
            errorMessage.text = context.getString(R.string.month_validation_error)
            errorMessage.visibility = View.VISIBLE
            monthInput.post { keyboardManager.showKeyboard(monthInput) }
        } else if (year == null) {
            errorMessage.text = context.getString(R.string.year_validation_error)
            errorMessage.visibility = View.VISIBLE
            yearInput.post { keyboardManager.showKeyboard(yearInput) }
        } else {
            if (isEthiopianCalendar && EthiopianDateHelper.isValidEthiopianBirthdate(year, month, day, clock)) {
                val internationalBirthdate = EthiopianDateHelper.ethiopianDateToInternationalDate(
                    EthiopianDateHelper.EthiopianDate(year, month, day), clock = clock
                )
                handleNewValue(internationalBirthdate, DateAccuracy.D, dialog)
                return
            }

            if (!isEthiopianCalendar && DateUtils.isValidBirthdate(year, month, day)) {
                handleNewValue(LocalDate.of(year, month, day), DateAccuracy.D, dialog)
                return
            }

            errorMessage.text = context.getString(R.string.invalid_date_error_message)
            errorMessage.visibility = View.VISIBLE
        }
    }
}

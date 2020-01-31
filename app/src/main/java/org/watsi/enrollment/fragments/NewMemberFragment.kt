package org.watsi.enrollment.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ScrollView
import android.widget.TextView
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_new_member.add_another_member
import kotlinx.android.synthetic.main.fragment_new_member.birthdate_dialog_field
import kotlinx.android.synthetic.main.fragment_new_member.card_id_field
import kotlinx.android.synthetic.main.fragment_new_member.container
import kotlinx.android.synthetic.main.fragment_new_member.done
import kotlinx.android.synthetic.main.fragment_new_member.gender_field
import kotlinx.android.synthetic.main.fragment_new_member.medical_record_number
import kotlinx.android.synthetic.main.fragment_new_member.medical_record_number_layout
import kotlinx.android.synthetic.main.fragment_new_member.name
import kotlinx.android.synthetic.main.fragment_new_member.name_layout
import kotlinx.android.synthetic.main.fragment_new_member.phone_number
import kotlinx.android.synthetic.main.fragment_new_member.photo_field
import kotlinx.android.synthetic.main.fragment_new_member.profession_spinner_field
import kotlinx.android.synthetic.main.fragment_new_member.relationship_to_member_container
import kotlinx.android.synthetic.main.fragment_new_member.relationship_to_member_spinner_field
import kotlinx.android.synthetic.main.fragment_new_member.relationship_to_member_static_layout
import org.threeten.bp.Clock
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.R
import org.watsi.enrollment.activities.MainActivity
import org.watsi.enrollment.activities.QrCodeActivity
import org.watsi.enrollment.activities.SavePhotoActivity
import org.watsi.enrollment.activities.ScanNewMemberCardActivity
import org.watsi.enrollment.activities.ScanNewMemberCardActivity.Companion.PARAM_ADDITIONAL_CARD_IDS_TO_EXCLUDE
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.PaymentType
import org.watsi.enrollment.domain.utils.StringUtils
import org.watsi.enrollment.flowstates.HouseholdFlowState
import org.watsi.enrollment.helpers.LayoutHelper
import org.watsi.enrollment.helpers.SnackbarHelper
import org.watsi.enrollment.helpers.StringHelper.getStringNullSafe
import org.watsi.enrollment.helpers.TranslationHelper
import org.watsi.enrollment.managers.KeyboardManager
import org.watsi.enrollment.managers.NavigationManager
import org.watsi.enrollment.viewmodels.NewMemberViewModel
import javax.inject.Inject

class NewMemberFragment : DaggerFragment(), TextView.OnEditorActionListener, NavigationManager.HandleOnBack {

    @Inject lateinit var logger: Logger
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var clock: Clock
    @Inject lateinit var sessionManager: SessionManager
    private lateinit var householdFlowState: HouseholdFlowState
    private var isHeadOfHousehold: Boolean = true
    private var newHousehold: Boolean = true
    private var memberIndex: Int = 0
    lateinit var viewModel: NewMemberViewModel

    companion object {
        const val PARAM_HOUSEHOLD_FLOWSTATE = "household_flowstate"
        const val PARAM_MEMBER_INDEX = "member_index"
        const val PARAM_NEW_HOUSEHOLD = "new_household"
        const val CAPTURE_PHOTO_INTENT = 1
        const val SCAN_QRCODE_INTENT = 3
        const val AUTO_SCROLL_PADDING_IN_DP = 60

        fun forHousehold(householdFlowState: HouseholdFlowState, memberIndex: Int, newHousehold: Boolean): NewMemberFragment {
            val newMemberFragment = NewMemberFragment()
            newMemberFragment.arguments = Bundle().apply {
                putSerializable(PARAM_HOUSEHOLD_FLOWSTATE, householdFlowState)
                putInt(PARAM_MEMBER_INDEX, memberIndex)
                putBoolean(PARAM_NEW_HOUSEHOLD, newHousehold)
            }
            return newMemberFragment
        }
    }

    private fun setErrors(errorMap: Map<String, Int?>) {
        name_layout.error = getStringNullSafe(errorMap[NewMemberViewModel.MEMBER_NAME_ERROR], context)
        errorMap[NewMemberViewModel.MEMBER_NAME_LENGTH_ERROR]?.let { stringResource ->
            name_layout.error = String.format(getString(stringResource), BuildConfig.MEMBER_FULL_NAME_MIN_LENGTH)
        }
        gender_field.setError(getStringNullSafe(errorMap[NewMemberViewModel.MEMBER_GENDER_ERROR], context))
        photo_field.setError(getStringNullSafe(errorMap[NewMemberViewModel.MEMBER_PHOTO_ERROR], context))
        birthdate_dialog_field.setErrorOnField(getStringNullSafe(errorMap[NewMemberViewModel.MEMBER_AGE_ERROR], context))
        profession_spinner_field.setError(getStringNullSafe(errorMap[NewMemberViewModel.MEMBER_PROFESSION_ERROR], context))
        relationship_to_member_spinner_field.setError(getStringNullSafe(errorMap[NewMemberViewModel.MEMBER_RELATIONSHIP_TO_HEAD_ERROR], context))
        medical_record_number_layout.error = getStringNullSafe(errorMap[NewMemberViewModel.MEMBER_MEDICAL_RECORD_NUMBER_ERROR], context)
    }

    private fun scrollToFirstError(errorMap: Map<String, Int>) {
        val validationKeysToField = linkedMapOf(
            NewMemberViewModel.MEMBER_GENDER_ERROR to gender_field,
            NewMemberViewModel.MEMBER_NAME_ERROR to name_layout,
            NewMemberViewModel.MEMBER_AGE_ERROR to birthdate_dialog_field,
            NewMemberViewModel.MEMBER_PHOTO_ERROR to photo_field
        )

        validationKeysToField.forEach {
            val validationKey = it.key
            val layout = it.value
            if (errorMap[validationKey] != null) {
                (view as ScrollView).smoothScrollTo(0, layout.top - AUTO_SCROLL_PADDING_IN_DP)
                return
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        householdFlowState = arguments.get(PARAM_HOUSEHOLD_FLOWSTATE) as HouseholdFlowState
        memberIndex = arguments.getInt(PARAM_MEMBER_INDEX)
        newHousehold = arguments.getBoolean(PARAM_NEW_HOUSEHOLD)
        isHeadOfHousehold = memberIndex == 0

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(NewMemberViewModel::class.java)
        viewModel.getViewStateObservable().observe(this, Observer {
            if (it != null) {
                setErrors(it.errors)

                when (it.status) {
                    NewMemberViewModel.MemberStatus.ERROR -> {
                        // TODO do the snackbar thing.
                        // TODO do the auto scroll thing.
                    }
                    NewMemberViewModel.MemberStatus.NEW -> {
                        // no-op
                    }
                    else -> {
                        // TODO: handle saving
                    }
                }

                if (it.name.isEmpty()) {
                    activity.title = if (isHeadOfHousehold) {
                        context.getString(R.string.new_household_title)
                    } else {
                        context.getString(R.string.member_details_title)
                    }
                } else {
                    activity.title = it.name
                }

                if (it.birthdate != null && it.birthdateAccuracy != null) {
                    birthdate_dialog_field.setValue(it.birthdate, it.birthdateAccuracy)
                }

                val photoId = it.photoId
                if (photoId != null && it.thumbnailPhoto != null) {
                    val thumbnailBitmap = BitmapFactory.decodeByteArray(
                        it.thumbnailPhoto.bytes, 0, it.thumbnailPhoto.bytes.size)
                    photo_field.setPhotoPreview(thumbnailBitmap)
                }

                it.cardId?.let {
                    card_id_field.setCardId(StringUtils.formatCardId(it))
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setToolbar(R.string.member_details_title, 0)
        return inflater?.inflate(R.layout.fragment_new_member, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        name.addTextChangedListener(LayoutHelper.OnChangedListener {
            text -> viewModel.onNameChange(text)
        })
        name.setOnEditorActionListener(this)

        birthdate_dialog_field.configureBirthdateDialog(keyboardManager, { birthdate, birthdateAccuracy, dialog ->
            viewModel.onBirthdateChange(birthdate)
            viewModel.onBirthdateAccuracyChange(birthdateAccuracy)
            dialog.dismiss()
        }, clock)

        phone_number.addTextChangedListener(LayoutHelper.OnChangedListener {
            text -> viewModel.onPhoneNumberChange(text)
        })

        gender_field.setOnGenderChange { gender -> viewModel.onGenderChange(gender) }

        listOf(name, medical_record_number).forEach {
            it.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) { keyboardManager.hideKeyboard(view) }
            }
        }

        if (BuildConfig.ENABLE_COLLECT_RELATIONSHIP_TO_HEAD) {
            relationship_to_member_container.visibility = View.VISIBLE

            val relationshipToHeadChoices = TranslationHelper.getTranslatedRelationshipToHeadOptions(context)

            if (isHeadOfHousehold) {
                relationship_to_member_static_layout.visibility = View.VISIBLE
                relationship_to_member_spinner_field.visibility = View.GONE
                viewModel.onRelationshipToHeadSelected(Member.RELATIONSHIP_TO_HEAD_SELF)
            } else {
                relationship_to_member_static_layout.visibility = View.GONE
                relationship_to_member_spinner_field.visibility = View.VISIBLE

                relationship_to_member_spinner_field.setUpWithPrompt(
                    choices = relationshipToHeadChoices.map { it.second },
                    initialChoice = null,
                    onItemSelected = { idx: Int -> viewModel.onRelationshipToHeadSelected(relationshipToHeadChoices[idx].first) },
                    promptString = getString(R.string.relationship_to_member_prompt),
                    onPromptSelected = { viewModel.onRelationshipToHeadSelected(null) }
                )
            }
        }

        if (BuildConfig.ENABLE_COLLECT_PROFESSION) {
            profession_spinner_field.visibility = View.VISIBLE
            val professionChoices = TranslationHelper.getTranslatedProfessionOptions(context)

            profession_spinner_field.setUpWithPrompt(
                choices = professionChoices.map { it.second },
                initialChoice = null,
                onItemSelected = { idx: Int -> viewModel.onProfessionSelected(professionChoices[idx].first) },
                promptString = getString(R.string.profession_prompt),
                onPromptSelected = { viewModel.onProfessionSelected(null) }
            )
        }

        if (BuildConfig.ENABLE_COLLECT_MEDICAL_RECORD_NUMBER) {
            medical_record_number_layout.visibility = View.VISIBLE
            medical_record_number.addTextChangedListener(LayoutHelper.OnChangedListener { text ->
                viewModel.onMedicalRecordNumberChange(text)
            })
            medical_record_number.setOnEditorActionListener(this)
        }

        photo_field.setOnClickListener {
            startActivityForResult(Intent(activity, SavePhotoActivity::class.java), CAPTURE_PHOTO_INTENT)
        }

        card_id_field.setOnClickListener {
            val intent = Intent(activity, ScanNewMemberCardActivity::class.java)
            intent.putStringArrayListExtra(
                PARAM_ADDITIONAL_CARD_IDS_TO_EXCLUDE,
                householdFlowState.cardIds()
            )
            startActivityForResult(intent, SCAN_QRCODE_INTENT)
        }

        if (!newHousehold) {
            add_another_member.visibility = View.GONE
        }
        add_another_member.setOnClickListener {
            viewModel.validateFields().subscribe({
                navigationManager.goTo(
                    NewMemberFragment.forHousehold(
                        viewModel.cacheMember(
                            householdFlowState,
                            memberIndex
                        ), memberIndex + 1, newHousehold
                    )
                )
            }, {
                showSnackbarForFieldErrors(it)
            })
        }

        done.setOnClickListener {
            viewModel.validateFields().subscribe({
                val user = sessionManager.currentAuthenticationToken()?.user!!
                val updatedHousehold = viewModel.cacheMember(householdFlowState, memberIndex)

                if (newHousehold) {
                    val paymentType = if (householdFlowState.manualMembershipNumber == null) {
                        PaymentType.NEW_ENROLLMENT
                    } else {
                        PaymentType.MANUAL_RENEWAL
                    }

                    // If payment info is collected, go to the payment fragment.
                    // Otherwise, go to the final review screen.
                    if (BuildConfig.ENABLE_COLLECT_PAYMENT_INFO) {
                        navigationManager.goTo(PaymentFragment.forHousehold(
                            householdFlowState = updatedHousehold,
                            paymentType = paymentType
                        ))
                    } else {
                        navigationManager.goTo(ReviewFragment.forHousehold(updatedHousehold))
                    }
                } else {
                    viewModel.saveMember(
                        updatedHousehold.members[memberIndex].id,
                        householdFlowState.toHouseholdWithMembersAndPayment(),
                        user
                    ).subscribe({
                        navigationManager.popTo(HouseholdFragment.forHousehold(updatedHousehold.household.id))
                    }, {
                        showSnackbarForFieldErrors(it)
                    })
                }
            } , {
                showSnackbarForFieldErrors(it)
            })
        }
    }

    /**
     * Define custom onEditorAction behavior for cases where we do not want the default ime behavior
     * when entering information into the inputs
     */
    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (v == null) return false
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (v == name) {
                v.clearFocus()
                keyboardManager.hideKeyboard(v)
                birthdate_dialog_field.performClick()
                return true
            }
        } else if (actionId == EditorInfo.IME_ACTION_DONE) {
            // override default IME_ACTION_DONE behavior which focuses the next focusable input
            // by requesting focus on the layout which effectively un-focuses any input
            container.requestFocus()
            keyboardManager.hideKeyboard(v)
            return true
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAPTURE_PHOTO_INTENT -> {
                val (photoIds, _) = SavePhotoActivity.parseResult(resultCode, data, logger)
                if (photoIds != null) {
                    viewModel.onPhotoTaken(photoIds.first, photoIds.second)
                }
            }
            SCAN_QRCODE_INTENT -> {
                val (cardId, _) = QrCodeActivity.parseResult(resultCode, data, logger)
                if (cardId != null) {
                    viewModel.onCardScan(cardId)
                }
            }
            else -> {
                logger.error("unknown request code")
            }
        }
    }

    private fun showSnackbarForFieldErrors(throwable: Throwable) {
        viewModel.errors()?.let {
            scrollToFirstError(it)
            SnackbarHelper.showError(done, context, throwable.localizedMessage)
        }
    }

    private fun clearMemberFromFlowState() {
        if (householdFlowState.members.size >= memberIndex) {
            householdFlowState.members = householdFlowState.members.subList(0, memberIndex)
        }
    }

    override fun onBack(): Single<Boolean> {
        // if no information has entered, do not show confirmation dialog onBack
        return if (viewModel.getViewStateObservable().value == NewMemberViewModel.ViewState()) {
            Single.just(true)
        } else {
            Single.create<Boolean> { single ->
                AlertDialog.Builder(activity)
                        .setTitle(R.string.exit_form_alert)
                        .setMessage(R.string.new_member_dialog_message)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            clearMemberFromFlowState()
                            single.onSuccess(true)
                        }
                        .setNegativeButton(R.string.cancel) { _, _ -> single.onSuccess(false) }
                        .setOnDismissListener { single.onSuccess(false) }
                        .show()
            }
        }
    }
}

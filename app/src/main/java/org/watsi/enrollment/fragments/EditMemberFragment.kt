package org.watsi.enrollment.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_edit_member.card_id_field
import kotlinx.android.synthetic.main.fragment_edit_member.edit_birthdate_dialog_field
import kotlinx.android.synthetic.main.fragment_edit_member.gender_field
import kotlinx.android.synthetic.main.fragment_edit_member.medical_record_number_field
import kotlinx.android.synthetic.main.fragment_edit_member.membership_number_field
import kotlinx.android.synthetic.main.fragment_edit_member.membership_number_layout
import kotlinx.android.synthetic.main.fragment_edit_member.name_field
import kotlinx.android.synthetic.main.fragment_edit_member.phone_number_field
import kotlinx.android.synthetic.main.fragment_edit_member.photo_container
import kotlinx.android.synthetic.main.fragment_edit_member.profession_spinner_field
import kotlinx.android.synthetic.main.fragment_edit_member.relationship_to_member_container
import kotlinx.android.synthetic.main.fragment_edit_member.relationship_to_member_spinner_field
import kotlinx.android.synthetic.main.fragment_edit_member.relationship_to_member_static_layout
import kotlinx.android.synthetic.main.fragment_edit_member.top_gender_age
import kotlinx.android.synthetic.main.fragment_edit_member.top_name
import kotlinx.android.synthetic.main.fragment_edit_member.top_photo
import org.threeten.bp.Clock
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.R
import org.watsi.enrollment.activities.MainActivity
import org.watsi.enrollment.activities.QrCodeActivity
import org.watsi.enrollment.activities.SavePhotoActivity
import org.watsi.enrollment.activities.ScanNewMemberCardActivity
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import org.watsi.enrollment.domain.usecases.ArchiveMemberUseCase
import org.watsi.enrollment.domain.usecases.LoadHouseholdUseCase
import org.watsi.enrollment.domain.usecases.UpdateMemberUseCase
import org.watsi.enrollment.domain.utils.StringUtils
import org.watsi.enrollment.helpers.PhotoLoader
import org.watsi.enrollment.helpers.SnackbarHelper
import org.watsi.enrollment.helpers.StringHelper
import org.watsi.enrollment.helpers.TranslationHelper
import org.watsi.enrollment.managers.KeyboardManager
import org.watsi.enrollment.managers.NavigationManager
import org.watsi.enrollment.viewmodels.EditMemberViewModel
import javax.inject.Inject

class EditMemberFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var clock: Clock
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var archiveMemberUseCase: ArchiveMemberUseCase
    @Inject lateinit var loadHouseholdUseCase: LoadHouseholdUseCase
    @Inject lateinit var updateMemberUseCase: UpdateMemberUseCase

    private lateinit var viewModel: EditMemberViewModel
    private lateinit var genderOptions: List<Pair<Gender, String>>
    private lateinit var professionOptions: List<Pair<String, String>>
    private lateinit var relationshipToHeadOptions: List<Pair<String, String>>

    private var placeholderPhotoIconPadding = 0

    companion object {
        const val PARAM_MEMBER = "member"
        const val CAPTURE_PHOTO_INTENT = 1
        const val SCAN_QRCODE_INTENT = 2

        fun forMember(member: Member): EditMemberFragment {
            val editMemberFragment = EditMemberFragment()
            editMemberFragment.arguments = Bundle().apply {
                putSerializable(PARAM_MEMBER, member)
            }
            return editMemberFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        placeholderPhotoIconPadding =resources.getDimensionPixelSize(R.dimen.editMemberPhotoPlaceholderPadding)

        genderOptions = TranslationHelper.getTranslatedGenderOptions(context)
        professionOptions = TranslationHelper.getTranslatedProfessionOptions(context)
        relationshipToHeadOptions = TranslationHelper.getTranslatedRelationshipToHeadOptions(context)

        val paramMember = arguments.get(PARAM_MEMBER) as Member
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(EditMemberViewModel::class.java)

        val observable = viewModel.getObservable(paramMember)
        observable.observe(this, Observer { viewState ->
            viewState?.memberWithThumbnail?.let { memberWithThumbnail ->
                val member = memberWithThumbnail.member
                activity.title = member.name
                top_name.text = member.name

                PhotoLoader.loadMemberPhoto(
                    bytes = memberWithThumbnail.photo?.bytes,
                    view = top_photo,
                    context = context,
                    gender = member.gender,
                    photoExists = member.photoExists(),
                    placeholderPadding = placeholderPhotoIconPadding
                )

                val genderString = genderOptions.find { it.first == member.gender }?.second
                top_gender_age.text = resources.getString(R.string.member_list_item_gender_age,
                        genderString,
                        StringHelper.getDisplayAge(member, context))

                name_field.setValue(member.name)
                gender_field.setValue(genderString)
                edit_birthdate_dialog_field.setValue(member.birthdate, member.birthdateAccuracy)
                phone_number_field.setValue(member.phoneNumber)

                member.cardId?.let { cardId ->
                    card_id_field.setCardId(StringUtils.formatCardId(cardId))
                }

                if (BuildConfig.ENABLE_COLLECT_MEMBERSHIP_NUMBER) {
                    membership_number_layout.visibility = View.VISIBLE
                    if (member.membershipNumber != null) {
                        membership_number_field.setText(member.membershipNumber)
                    } else {
                        membership_number_field.setText(R.string.blank_membership_number)
                    }
                }

                if (BuildConfig.ENABLE_COLLECT_MEDICAL_RECORD_NUMBER) {
                    medical_record_number_field.visibility = View.VISIBLE
                    medical_record_number_field.setValue(member.medicalRecordNumber)
                }

                if (BuildConfig.ENABLE_COLLECT_RELATIONSHIP_TO_HEAD) {
                    relationship_to_member_container.visibility = View.VISIBLE
                    if (member.isHeadOfHousehold()) {
                        relationship_to_member_static_layout.visibility = View.VISIBLE
                        relationship_to_member_spinner_field.visibility = View.GONE
                    } else {
                        val relationshipToHeadString = relationshipToHeadOptions.find { it.first == member.relationshipToHead }?.second
                        relationship_to_member_spinner_field.setValue(relationshipToHeadString)
                    }
                }

                if (BuildConfig.ENABLE_COLLECT_PROFESSION) {
                    profession_spinner_field.visibility = View.VISIBLE

                    // TODO -- Move this logic into a method for translation helper.
                    val professionString = professionOptions.find { it.first == member.profession }?.second
                    profession_spinner_field.setValue(professionString ?: member.profession)
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setToolbar(R.string.blank, 0)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_edit_member, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        name_field.configureEditTextDialog(
            keyboardManager = keyboardManager,
            handleNewValue = { name, dialog ->
                viewModel.updateName(name).subscribe(UpdateFieldObserver(dialog))
            },
            validateFieldAndReturnError = { name ->
                viewModel.validateName(name, getString(R.string.name_length_validation_error))
            }
        )

        gender_field.configureOptionsDialog(genderOptions.map { it.second }.toTypedArray(), { idx ->
            viewModel.updateGender(genderOptions[idx].first).subscribe()
        })

        edit_birthdate_dialog_field.configureBirthdateDialog(keyboardManager, { birthdate, birthdateAccuracy, dialog ->
            viewModel.updateBirthdate(birthdate, birthdateAccuracy)
                    .subscribe(UpdateFieldObserver(dialog, R.id.age_input_layout))
        }, clock)

        phone_number_field.configureEditTextDialog(keyboardManager, { phoneNumberString, dialog ->
            viewModel.updatePhoneNumber(phoneNumberString).subscribe(UpdateFieldObserver(dialog))
        })

        medical_record_number_field.configureEditTextDialog(
            keyboardManager = keyboardManager,
            handleNewValue = { medicalRecordNumberString, dialog ->
                viewModel.updateMedicalRecordNumber(medicalRecordNumberString)
                        .subscribe(UpdateFieldObserver(dialog))
            },
            validateFieldAndReturnError = { medicalRecordNumberString ->
                viewModel.validateMedicalRecordNumber(medicalRecordNumberString,
                    String.format(
                        getString(R.string.medical_record_number_validation_error),
                        BuildConfig.MEMBER_MEDICAL_RECORD_NUMBER_MIN_LENGTH,
                        BuildConfig.MEMBER_MEDICAL_RECORD_NUMBER_MAX_LENGTH
                    )
                )
            },
            maxTextLength = BuildConfig.MEMBER_MEDICAL_RECORD_NUMBER_MAX_LENGTH
        )

        profession_spinner_field.configureOptionsDialog(
            professionOptions.map { it.second }.toTypedArray(), { idx ->
                viewModel.updateProfession(professionOptions[idx].first).subscribe()
            }
        )

        relationship_to_member_spinner_field.configureOptionsDialog(
            relationshipToHeadOptions.map { it.second }.toTypedArray(), { idx ->
                viewModel.updateRelationshipToHead(relationshipToHeadOptions[idx].first).subscribe()
            }
        )

        photo_container.setOnClickListener {
            startActivityForResult(Intent(activity, SavePhotoActivity::class.java), CAPTURE_PHOTO_INTENT)
        }

        card_id_field.setOnClickListener {
            startActivityForResult(Intent(activity, ScanNewMemberCardActivity::class.java), SCAN_QRCODE_INTENT)
        }
    }

    /**
     * CompletableObserver for dismissing the open AlertDialog if successful or displaying an
     * error message if an error occurs
     */
    inner class UpdateFieldObserver(private val dialog: AlertDialog,
                                    private val layoutId: Int = R.id.dialog_input_layout
    ) : CompletableObserver {
        override fun onComplete() {
            dialog.dismiss()
        }

        override fun onSubscribe(d: Disposable) { /* no-op */ }

        override fun onError(e: Throwable) {
            val layout = dialog.findViewById<TextInputLayout>(layoutId)
            layout?.error = e.localizedMessage
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAPTURE_PHOTO_INTENT -> {
                val (photoIds, _) = SavePhotoActivity.parseResult(resultCode, data, logger)
                if (photoIds != null) {
                    viewModel.updatePhoto(photoIds.first, photoIds.second).subscribe()
                }
            }
            SCAN_QRCODE_INTENT -> {
                val (cardId, _) = QrCodeActivity.parseResult(resultCode, data, logger)
                if (cardId != null) {
                    viewModel.updateMemberCard(cardId).subscribe()
                }
            }
            else -> {
                logger.error("Unknown requestCode called from EditMemberFragment: $requestCode")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_delete_beneficiary -> {
                handleDelete()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFailedToArchiveError() {
        view?.let {
            SnackbarHelper.showError(it, context, "Error")
        }
    }

    private fun handleDelete() {
        viewModel.sourceLiveData?.value?.memberWithThumbnail?.let { memberWithThumbnail ->
            val member = memberWithThumbnail.member
            val household = loadHouseholdUseCase.execute(member.householdId).blockingFirst()
            val remainingBeneficiaries = household.unarchivedMembers().filterNot { it == memberWithThumbnail }
            val lastMember = remainingBeneficiaries.isEmpty()
            val isHeadOfHousehold = member.isHeadOfHousehold()
            val deleteChoices = TranslationHelper.getTranslatedMemberDeleteReasonOptions(context)
            val deleteChoicesAsTypedArray = deleteChoices.map { it.second }.toTypedArray()
            AlertDialog.Builder(activity)
                .setTitle(R.string.delete_dialog_message)
                .setItems(deleteChoicesAsTypedArray, { _, which ->
                    val archivedReasonPair = deleteChoices[which]
                    val archiveReasonEnum = archivedReasonPair.first
                    when {
                        lastMember -> {
                            confirmDeleteHouseholdDialog(member, archiveReasonEnum)
                        }
                        isHeadOfHousehold -> {
                            launchReassignMemberDialog(
                                memberWithThumbnail,
                                remainingBeneficiaries,
                                archiveReasonEnum
                            )
                        }
                        else -> {
                            archiveMemberUseCase.execute(member, archiveReasonEnum, clock).subscribe({
                                navigationManager.popTo(HouseholdFragment.forHousehold(member.householdId))
                            }, {
                                logger.error(it)
                                showFailedToArchiveError()
                            })
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show()
        } ?: run {
            showFailedToArchiveError()
        }
    }

    private fun confirmDeleteHouseholdDialog(member: Member, archiveReason: String) {
        AlertDialog.Builder(activity)
                .setTitle(R.string.confirm_household_delete_title)
                .setMessage(R.string.confirm_household_delete_message)
                .setPositiveButton(R.string.ok) { _, _ ->
                    archiveMemberUseCase.execute(member, archiveReason, clock).subscribe({
                        navigationManager.popTo(HomeFragment())
                    }, {
                        logger.error(it)
                        showFailedToArchiveError()
                    })
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    private fun launchReassignMemberDialog(
        memberWithThumbnail: MemberWithThumbnail,
        beneficiaries: List<MemberWithThumbnail>,
        archiveReason: String
    ) {
        val beneficiariesNames = beneficiaries.map { it.member.name }.toTypedArray()
        AlertDialog.Builder(activity)
                .setTitle(R.string.choose_member_dialog_message)
                .setItems(beneficiariesNames, { _, which ->
                    val member = memberWithThumbnail.member
                    val newMember = beneficiaries[which].member
                    val completables = listOf(
                        archiveMemberUseCase.execute(member, archiveReason, clock),
                        updateMemberUseCase.execute(newMember.copy(relationshipToHead = Member.RELATIONSHIP_TO_HEAD_SELF))
                    )
                    Completable.concat(completables).subscribe({
                        navigationManager.popTo(HouseholdFragment.forHousehold(member.householdId))
                    }, {
                        showFailedToArchiveError()
                    })
                })
                .setNegativeButton(R.string.cancel, null)
                .show()
    }
}

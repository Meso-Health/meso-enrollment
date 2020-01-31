package org.watsi.enrollment.activities

import android.content.Intent
import android.os.Bundle
import io.reactivex.android.schedulers.AndroidSchedulers
import org.watsi.enrollment.R
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.usecases.LoadMembersWithCardUseCase
import org.watsi.enrollment.helpers.StringHelper.truncateWithEllipses
import javax.inject.Inject

class ScanNewMemberCardActivity : QrCodeActivity() {
    @Inject lateinit var logger: Logger
    @Inject lateinit var loadMembersWithCardUseCase: LoadMembersWithCardUseCase
    private lateinit var additionalCardIdsToExclude: List<String>

    companion object {
        const val PARAM_ADDITIONAL_CARD_IDS_TO_EXCLUDE = "additional_card_ids_to_exclude"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        additionalCardIdsToExclude = intent.getStringArrayListExtra(PARAM_ADDITIONAL_CARD_IDS_TO_EXCLUDE).orEmpty()
    }

    override fun onActivityCreated() {
        setMessage(getString(R.string.scan_qr_code_help_text))
    }

    override fun onDetectedQrCode(qrCode: String) {
        if (Member.isValidCardId(qrCode)) {
            loadMembersWithCardUseCase.execute(qrCode)
                    .isEmpty
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { noPersistedMembersWithCard ->
                        if (noPersistedMembersWithCard && !additionalCardIdsToExclude.contains(qrCode)) {
                            val resultIntent = Intent().apply {
                                putExtra(QR_CODE_RESULT_KEY, qrCode)
                            }
                            setResult(RESULT_OK, resultIntent)
                            vibrate()
                            finish()
                        } else {
                            setErrorMessage(getString(R.string.card_already_assigned_error))
                        }
                    }
        } else {
            logger.warning("Invalid card ID scanned", mapOf(Pair("cardId", qrCode)))
            setErrorMessage(getString(R.string.invalid_card_error, truncateWithEllipses(qrCode)))
        }
    }

    override fun finishAsFailure(failureCode: Int) {
        val resultIntent = Intent()
        setResult(failureCode, resultIntent)
        finish()
    }
}

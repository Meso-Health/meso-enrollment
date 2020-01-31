package org.watsi.enrollment.activities

import android.content.Intent
import io.reactivex.MaybeObserver
import io.reactivex.disposables.Disposable
import org.watsi.enrollment.R
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.usecases.LoadMembersWithCardUseCase
import org.watsi.enrollment.helpers.StringHelper
import javax.inject.Inject

class SearchHouseholdByCardActivity : QrCodeActivity() {
    @Inject lateinit var logger: Logger
    companion object {
        const val HOUSEHOLD_ID_KEY = "household_id"
        const val CARD_ID_KEY = "card_id"
    }

    @Inject lateinit var loadMembersWithCardUseCase: LoadMembersWithCardUseCase

    override fun onActivityCreated() {
        setMessage(getString(R.string.search_by_member_card))
    }

    override fun onDetectedQrCode(qrCode: String) {
        if (Member.isValidCardId(qrCode)) {
            loadMembersWithCardUseCase.execute(qrCode).subscribe(object: MaybeObserver<Member> {
                override fun onSubscribe(d: Disposable) { Unit }
                override fun onError(e: Throwable) { logger.error(e) }

                // Card not found locally
                override fun onComplete() {
                    val resultIntent = Intent().apply {
                        putExtra(CARD_ID_KEY, qrCode)
                    }
                    setResult(RESULT_NOT_FOUND, resultIntent)
                    finish()
                }

                override fun onSuccess(member: Member) {
                    val resultIntent = Intent().apply {
                        putExtra(HOUSEHOLD_ID_KEY, member.householdId)
                    }
                    setResult(RESULT_OK, resultIntent)
                    vibrate()
                    finish()
                }
            })
        } else {
            logger.warning("Invalid card ID scanned", mapOf(Pair("cardId", qrCode)))
            setErrorMessage(getString(R.string.invalid_card_error, StringHelper.truncateWithEllipses(qrCode)))
        }
    }

    override fun finishAsFailure(failureCode: Int) {
        val resultIntent = Intent()
        setResult(failureCode, resultIntent)
        finish()
    }
}

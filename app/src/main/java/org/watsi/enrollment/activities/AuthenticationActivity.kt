package org.watsi.enrollment.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_authentication.android_version
import kotlinx.android.synthetic.main.activity_authentication.app_version
import kotlinx.android.synthetic.main.activity_authentication.login_button
import kotlinx.android.synthetic.main.activity_authentication.login_password
import kotlinx.android.synthetic.main.activity_authentication.login_username
import kotlinx.android.synthetic.main.activity_authentication.password_container
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.R
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.usecases.DeleteUserDataUseCase
import org.watsi.enrollment.domain.usecases.FetchAdministrativeDivisionsUseCase
import org.watsi.enrollment.domain.usecases.FetchEnrollmentPeriodsUseCase
import org.watsi.enrollment.domain.usecases.VerifyCurrentEnrollmentPeriodExistsUseCase
import org.watsi.enrollment.helpers.NetworkErrorHelper
import org.watsi.enrollment.managers.KeyboardManager
import javax.inject.Inject

class AuthenticationActivity : LocaleAwareActivity() {
    @Inject lateinit var logger: Logger
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var deleteUserDataUseCase: DeleteUserDataUseCase
    @Inject lateinit var fetchAdministrativeDivisionsUseCase: FetchAdministrativeDivisionsUseCase
    @Inject lateinit var fetchEnrollmentPeriodsUseCase: FetchEnrollmentPeriodsUseCase
    @Inject lateinit var verifyCurrentEnrollmentPeriodExistsUseCase: VerifyCurrentEnrollmentPeriodExistsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (sessionManager.currentAuthenticationToken()?.token != null) {
            navigateToMainActivity()
        }

        setTitle(R.string.enrollment)
        setContentView(R.layout.activity_authentication)

        login_button.setOnClickListener {
            login_username.clearFocus()
            login_password.clearFocus()
            keyboardManager.hideKeyboard(it)

            login_button.text = getString(R.string.logging_in)
            login_button.isEnabled = false

            Completable.concatArray(
                sessionManager.login(login_username.text.toString(), login_password.text.toString()),
                Completable.fromAction {
                    if (sessionManager.shouldClearUserData()) {
                        deleteUserDataUseCase.execute().blockingAwait()
                    }
                }.onErrorComplete {
                    logger.error(it)
                    // In the error case, we want to complete anyway since the user can still do what they
                    // need to do (enroll households) without the error deleted. Returning true achieves this behavior.
                    true
                },
                fetchEnrollmentPeriodsUseCase.execute(),
                fetchAdministrativeDivisionsUseCase.execute(),
                verifyCurrentEnrollmentPeriodExistsUseCase.execute() // This will return an exception if no current enrollment period exists.
            ).observeOn(AndroidSchedulers.mainThread()).subscribe({
                navigateToMainActivity()
            }, this::handleLoginFailure)
        }

        login_password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login_button.performClick()
            }
            false
        }

        app_version.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
        android_version.text = getString(R.string.android_version, android.os.Build.VERSION.RELEASE)
    }

    private fun handleLoginFailure(throwable: Throwable) {
        val throwable = throwable.cause ?: throwable

        when {
            throwable is VerifyCurrentEnrollmentPeriodExistsUseCase.NoEnrollmentPeriodFoundException -> {
                password_container.error = getString(R.string.enrollment_data_misconfigured_message)
            }
            throwable is SessionManager.PermissionException -> {
                password_container.error = getString(R.string.login_permission_error)
            }
            NetworkErrorHelper.isHttpUnauthorized(throwable) -> {
                password_container.error = getString(R.string.login_wrong_username_or_password_message)
            }
            NetworkErrorHelper.isPhoneOfflineError(throwable) -> {
                password_container.error = getString(R.string.login_phone_offline_error)
            }
            NetworkErrorHelper.isServerOfflineError(throwable) -> {
                password_container.error = getString(R.string.login_server_offline_error)
            }
            NetworkErrorHelper.isPoorConnectivityError(throwable) -> {
                password_container.error = getString(R.string.login_connectivity_error)
            }
            else -> {
                // login failed due to server error
                //  this path should only be used for server 500s, if we are seeing
                //  exceptions for other reasons being caught here, we should add
                //  them to the appropriate cases above
                password_container.error = getString(R.string.login_generic_failure_message)
                logger.error(throwable)
            }
        }
        login_button.text = getString(R.string.login_button_text)
        login_button.isEnabled = true

        // login may have succeeded so need to clear token if fetch fails
        sessionManager.logout()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (BuildConfig.ENABLE_LANGUAGE_SWITCH) {
            menuInflater.inflate(R.menu.main, menu)
            val switchLanguageItem = menu.findItem(R.id.menu_switch_language)
            switchLanguageItem.isVisible = true
        }
        return true
    }

    override fun onBackPressed() {
        // no-op to ensure if user goes back on this screen, they stay on this screen instead of exiting the app.
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_switch_language -> {
                localeManager.setLocaleConfirmationDialog(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

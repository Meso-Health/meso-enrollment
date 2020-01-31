package org.watsi.enrollment.activities

import android.Manifest
import android.app.AlertDialog
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.app.ActivityCompat
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import kotlinx.android.synthetic.main.view_dialog_edit_field.view.field_label
import org.watsi.enrollment.R
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.fragments.HomeFragment
import org.watsi.enrollment.managers.NavigationManager
import org.watsi.enrollment.services.BaseService
import org.watsi.enrollment.services.FetchService
import org.watsi.enrollment.services.SyncDataService
import org.watsi.enrollment.services.SyncPhotosService
import java.util.Locale
import javax.inject.Inject

class MainActivity : LocaleAwareActivity() {
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var navigationManager: NavigationManager

    companion object {
        const val SYNC_RECORDS_JOB_ID = 1
        const val SYNC_PHOTOS_JOB_ID = 2
        const val FETCH_RECORDS_JOB_ID = 3
        val requiredPermissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startServices()
        navigationManager.goTo(HomeFragment())
    }

    override fun onPostResume() {
        super.onPostResume()

        val hasPermissions = requiredPermissions.all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!hasPermissions) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 0)
        } else if (sessionManager.currentAuthenticationToken()?.token == null) {
            navigateToAuthenticationActivity()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_switch_language -> {
                localeManager.setLocaleConfirmationDialog(this)
                true
            }
            android.R.id.home -> {
                navigationManager.goBack()
                return true
            }
            R.id.menu_logout -> {
                AlertDialog.Builder(this)
                        .setTitle(R.string.log_out_alert)
                        .setMessage(R.string.log_out_alert_message)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                            jobScheduler.cancelAll()
                            sessionManager.logout()
                            navigateToAuthenticationActivity()
                        }.create().show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Helper method for configuring the toolbar from a Fragment
     *
     * @param titleId ID of a StringRes to use as the title
     * @param homeIconId ID of a DrawableRes to use as the up navigation affordance
     *                   Pass null if an up navigation affordance should not be displayed
     *                   and pass 0 to use the theme default (back arrow)
     */
    fun setToolbar(@StringRes titleId: Int, @DrawableRes homeIconId: Int?) {
        setTitle(titleId)
        setToolbarHomeIcon(homeIconId)
    }

    /**
     * Sets the toolbar to be flat and title-less.
     */
    fun setToolbarMinimal(@DrawableRes homeIconId: Int?) {
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.elevation = 0.toFloat()
        setToolbarHomeIcon(homeIconId)
    }

    /**
     * Resets the changes made by setToolbarMinimal
     */
    fun resetToolbarMinimal() {
        supportActionBar?.setDisplayShowTitleEnabled(true)
        //TODO: 4 is the default material design elevation for action bars, but ours is set to 12 for some reason.
        supportActionBar?.elevation = 12.toFloat()
    }

    /**
     * Kicks off background services
     */
    fun startServices() {
        BaseService.schedule(SYNC_RECORDS_JOB_ID, this, SyncDataService::class.java)
        BaseService.schedule(FETCH_RECORDS_JOB_ID, this, FetchService::class.java)
        BaseService.schedule(SYNC_PHOTOS_JOB_ID, this, SyncPhotosService::class.java)
    }

    private fun setToolbarHomeIcon(@DrawableRes homeIconId: Int?) {
        supportActionBar?.setDisplayHomeAsUpEnabled(homeIconId != null)
        homeIconId?.let{ supportActionBar?.setHomeAsUpIndicator(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onBackPressed() {
        navigationManager.goBack()
    }

    fun navigateToAuthenticationActivity() {
        startActivity(Intent(this, AuthenticationActivity::class.java))
        finish()
    }

    fun setSoftInputModeToResize() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    fun setSoftInputModeToPan() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }
}

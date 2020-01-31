package org.watsi.enrollment.fragments

import android.app.AlertDialog
import android.app.job.JobScheduler
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_status.android_version
import kotlinx.android.synthetic.main.fragment_status.app_version
import kotlinx.android.synthetic.main.fragment_status.current_user
import kotlinx.android.synthetic.main.fragment_status.data_last_synced_at
import kotlinx.android.synthetic.main.fragment_status.fetch_error
import kotlinx.android.synthetic.main.fragment_status.fetch_progress_bar
import kotlinx.android.synthetic.main.fragment_status.last_fetched_administrative_divisions
import kotlinx.android.synthetic.main.fragment_status.last_fetched_at
import kotlinx.android.synthetic.main.fragment_status.last_fetched_households
import kotlinx.android.synthetic.main.fragment_status.last_fetched_member_photos
import kotlinx.android.synthetic.main.fragment_status.photos_last_synced_at
import kotlinx.android.synthetic.main.fragment_status.sync_data_error
import kotlinx.android.synthetic.main.fragment_status.sync_data_progress_bar
import kotlinx.android.synthetic.main.fragment_status.sync_photos_error
import kotlinx.android.synthetic.main.fragment_status.sync_photos_progress_bar
import kotlinx.android.synthetic.main.fragment_status.unsynced_household_enrollment_records
import kotlinx.android.synthetic.main.fragment_status.unsynced_households
import kotlinx.android.synthetic.main.fragment_status.unsynced_members
import kotlinx.android.synthetic.main.fragment_status.unsynced_payments
import kotlinx.android.synthetic.main.fragment_status.unsynced_member_enrollment_records
import kotlinx.android.synthetic.main.fragment_status.unsynced_member_photos
import org.watsi.enrollment.BuildConfig
import org.watsi.enrollment.R
import org.watsi.enrollment.activities.MainActivity
import org.watsi.enrollment.device.managers.NetworkManager
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.services.BaseService
import org.watsi.enrollment.services.FetchService
import org.watsi.enrollment.services.SyncDataService
import org.watsi.enrollment.services.SyncPhotosService
import org.watsi.enrollment.viewmodels.StatusViewModel
import javax.inject.Inject

class StatusFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var networkManager: NetworkManager
    lateinit var viewModel: StatusViewModel
    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver()

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(StatusViewModel::class.java)

        viewModel.getNetworkObservable().observe(this, Observer {
            it?.let { viewState ->
                refreshNetworkIndicators(viewState.isFetching, viewState.fetchErrors, fetch_progress_bar, fetch_error)
                refreshNetworkIndicators(viewState.isDataSyncing, viewState.dataSyncErrors, sync_data_progress_bar, sync_data_error)
                refreshNetworkIndicators(viewState.isPhotoSyncing, viewState.photoSyncErrors, sync_photos_progress_bar, sync_photos_error)
            }
        })

        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->
                last_fetched_at.setValue(formattedUpdatedAt(viewState.fetchedAt.toEpochMilli()))
                data_last_synced_at.setValue(formattedUpdatedAt(viewState.dataSyncedAt.toEpochMilli()))
                photos_last_synced_at.setValue(formattedUpdatedAt(viewState.photoSyncedAt.toEpochMilli()))

                last_fetched_administrative_divisions.setValue(formattedUpdatedAt(viewState.administrativeDivisionsFetchedAt.toEpochMilli()))
                last_fetched_households.setValue(formattedUpdatedAt(viewState.householdsFetchedAt.toEpochMilli()))
                if (viewState.photosToFetchCount == 0) {
                    last_fetched_member_photos.setValue(formattedUpdatedAt(viewState.memberPhotosFetchedAt.toEpochMilli()))
                } else {
                    last_fetched_member_photos.setValue(formattedFetchQuantity(viewState.photosToFetchCount))
                }

                unsynced_households.setValue(formattedSyncQuantity(viewState.syncStatus.unsyncedHouseholdsCount))
                unsynced_household_enrollment_records.setValue(formattedSyncQuantity(viewState.syncStatus.unsyncedHouseholdEnrollmentRecordsCount))
                unsynced_members.setValue(formattedSyncQuantity(viewState.syncStatus.unsyncedMembersCount))
                unsynced_member_enrollment_records.setValue(formattedSyncQuantity(viewState.syncStatus.unsyncedMemberEnrollmentRecordsCount))
                unsynced_member_photos.setValue(formattedSyncQuantity(viewState.syncStatus.unsyncedPhotosCount))
                unsynced_payments.setValue(formattedSyncQuantity(viewState.syncStatus.unsyncedMembershipPaymentsCount))
            }
        })
    }

    private fun registerReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val className = intent.getStringExtra(BaseService.PARAM_SERVICE_CLASS)
                val isRunning = intent.getBooleanExtra(BaseService.PARAM_IS_RUNNING, false)
                val errors = intent.getStringArrayListExtra(BaseService.PARAM_ERRORS).orEmpty()

                when (className) {
                    FetchService::class.java.toString() -> viewModel.updateFetchStatus(isRunning, errors)
                    SyncDataService::class.java.toString() -> viewModel.updateSyncDataStatus(isRunning, errors)
                    SyncPhotosService::class.java.toString() -> viewModel.updateSyncPhotosStatus(isRunning, errors)
                }
            }
        }
        context.registerReceiver(broadcastReceiver, IntentFilter(BaseService.ACTION_SERVICE_UPDATE))
    }

    private fun refreshNetworkIndicators(isRunning: Boolean, errorMessages: List<String>, progressBar: View, errorIcon: View) {
        if (isRunning) {
            progressBar.visibility = View.VISIBLE
            errorIcon.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE

            if (errorMessages.isNotEmpty()) {
                errorIcon.setOnClickListener {
                    AlertDialog.Builder(activity).setMessage(errorMessages.joinToString("\n\n")).create().show()
                }
                errorIcon.visibility = View.VISIBLE
            } else {
                errorIcon.visibility = View.GONE
            }
        }
    }

    private fun formattedUpdatedAt(updatedAt: Long): String {
        return if (updatedAt == 0L) {
            getString(R.string.never)
        } else {
            DateUtils.getRelativeTimeSpanString(updatedAt).toString()
        }
    }

    private fun formattedSyncQuantity(count: Int): String {
        return if (count == 0) {
            getString(R.string.all_synced)
        } else {
            "$count ${getString(R.string.waiting_to_sync)}"
        }
    }

    private fun formattedFetchQuantity(count: Int): String {
        return if (count == 0) {
            getString(R.string.all_fetched)
        } else {
            "$count ${getString(R.string.waiting_to_fetch)}"
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setToolbar(R.string.menu_status_label, 0)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_status, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val username = sessionManager.currentAuthenticationToken()?.user?.username
        current_user.setValue(username)
        BuildConfig.VERSION_CODE
        app_version.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
        android_version.text = getString(R.string.android_version, android.os.Build.VERSION.RELEASE)
    }

    override fun onDestroy() {
        super.onDestroy()
        context.unregisterReceiver(broadcastReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.status, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_sync_now -> {
                val requireNetwork = BuildConfig.BUILD_TYPE != "debug"

                if (requireNetwork && !networkManager.isNetworkAvailable()) {
                    AlertDialog.Builder(activity)
                            .setTitle(getString(R.string.no_internet_title))
                            .setMessage(getString(R.string.no_internet_prompt))
                            .setPositiveButton(R.string.ok) { _, _ ->
                                // no-op
                            }
                            .create().show()
                } else {
                    // explicitly toggle spinners when user presses "sync now" button to avoid
                    // visual delay before spinners are toggled from job services actually being kicked off
                    viewModel.updateFetchStatus(true)
                    viewModel.updateSyncDataStatus(true)
                    viewModel.updateSyncPhotosStatus(true)
                    val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                    jobScheduler.cancelAll()
                    (activity as MainActivity).startServices()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

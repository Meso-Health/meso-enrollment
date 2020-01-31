package org.watsi.enrollment.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Flowable
import org.threeten.bp.Instant
import org.watsi.enrollment.device.managers.PreferencesManager
import org.watsi.enrollment.domain.repositories.DeltaRepository
import org.watsi.enrollment.domain.usecases.FetchStatusUseCase
import org.watsi.enrollment.domain.usecases.SyncStatusUseCase
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StatusViewModel @Inject constructor (
    private val fetchStatusUseCase: FetchStatusUseCase,
    private val syncStatusUseCase: SyncStatusUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val networkViewStateObservable = MutableLiveData<NetworkViewState>()

    fun getObservable(): LiveData<ViewState> {
        val flowables = listOf(
            fetchStatusUseCase.execute(),
            syncStatusUseCase.execute(),
            Flowable.interval(0, 1, TimeUnit.MINUTES) // allows status screen timestamps to keep updating
        )

        return LiveDataReactiveStreams.fromPublisher(
            Flowable.combineLatest(flowables) { results ->
                ViewState(
                    fetchedAt = preferencesManager.getLastFetched(),
                    administrativeDivisionsFetchedAt = preferencesManager.getAdministrativeDivisionsLastFetched(),
                    householdsFetchedAt = preferencesManager.getHouseholdsLastFetched(),
                    memberPhotosFetchedAt = preferencesManager.getMemberPhotosLastFetched(),
                    photosToFetchCount = results[0] as Int,
                    dataSyncedAt = preferencesManager.getDataLastSynced(),
                    photoSyncedAt = preferencesManager.getPhotoLastSynced(),
                    syncStatus = results[1] as DeltaRepository.SyncStatus
                )
            }
        )
    }

    fun getNetworkObservable(): LiveData<NetworkViewState> {
        networkViewStateObservable.value = NetworkViewState()
        return networkViewStateObservable
    }

    fun updateFetchStatus(isRunning: Boolean, fetchErrors: List<String> = emptyList()) {
        networkViewStateObservable.value = networkViewStateObservable.value?.copy(isFetching = isRunning, fetchErrors = fetchErrors)
    }

    fun updateSyncDataStatus(isRunning: Boolean, dataSyncErrors: List<String> = emptyList()) {
        networkViewStateObservable.value = networkViewStateObservable.value?.copy(isDataSyncing = isRunning, dataSyncErrors = dataSyncErrors)
    }

    fun updateSyncPhotosStatus(isRunning: Boolean, photoSyncErrors: List<String> = emptyList()) {
        networkViewStateObservable.value = networkViewStateObservable.value?.copy(isPhotoSyncing = isRunning, photoSyncErrors = photoSyncErrors)
    }

    data class NetworkViewState(
        val isFetching: Boolean = false,
        val isDataSyncing: Boolean = false,
        val isPhotoSyncing: Boolean = false,
        val fetchErrors: List<String> = emptyList(),
        val dataSyncErrors: List<String> = emptyList(),
        val photoSyncErrors: List<String> = emptyList()
    )

    data class ViewState(
        val fetchedAt: Instant,
        val administrativeDivisionsFetchedAt: Instant,
        val householdsFetchedAt: Instant,
        val memberPhotosFetchedAt: Instant,
        val photosToFetchCount: Int,
        val dataSyncedAt: Instant,
        val photoSyncedAt: Instant,
        val syncStatus: DeltaRepository.SyncStatus
    )
}

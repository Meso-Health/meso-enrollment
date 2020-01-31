package org.watsi.enrollment.services

import io.reactivex.Completable
import org.threeten.bp.Clock
import org.watsi.enrollment.R
import org.watsi.enrollment.device.managers.PreferencesManager
import org.watsi.enrollment.domain.usecases.SyncPhotoUseCase
import javax.inject.Inject

class SyncPhotosService : BaseService() {

    @Inject lateinit var syncPhotoUseCase: SyncPhotoUseCase
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var clock: Clock

    override fun executeTasks(): Completable {
        return Completable.concatArray(
            syncPhotoUseCase.execute { setError(it, getString(R.string.sync_member_photos_error_label)) },
            Completable.fromAction {
                if (getErrorMessages().isEmpty()) {
                    preferencesManager.updatePhotoLastSynced(clock.instant())
                }
            }
        )
    }
}

package org.watsi.enrollment.services

import io.reactivex.Completable
import org.threeten.bp.Clock
import org.watsi.enrollment.R
import org.watsi.enrollment.device.managers.PreferencesManager
import org.watsi.enrollment.domain.usecases.SyncHouseholdEnrollmentRecordUseCase
import org.watsi.enrollment.domain.usecases.SyncHouseholdUseCase
import org.watsi.enrollment.domain.usecases.SyncMemberEnrollmentRecordUseCase
import org.watsi.enrollment.domain.usecases.SyncMemberUseCase
import org.watsi.enrollment.domain.usecases.SyncMembershipPaymentUseCase
import javax.inject.Inject

class SyncDataService : BaseService() {

    @Inject lateinit var syncHouseholdUseCase: SyncHouseholdUseCase
    @Inject lateinit var syncMemberUseCase: SyncMemberUseCase
    @Inject lateinit var syncHouseholdEnrollmentRecordUseCase: SyncHouseholdEnrollmentRecordUseCase
    @Inject lateinit var syncMemberEnrollmentRecordUseCase: SyncMemberEnrollmentRecordUseCase
    @Inject lateinit var syncMembershipPaymentUseCase: SyncMembershipPaymentUseCase
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var clock: Clock

    override fun executeTasks(): Completable {
        return Completable.concatArray(
            syncHouseholdUseCase.execute { setError(it, getString(R.string.sync_households_error_label)) },
            syncMemberUseCase.execute { setError(it, getString(R.string.sync_members_error_label)) },
            syncHouseholdEnrollmentRecordUseCase.execute { setError(it, getString(R.string.sync_household_enrollment_records_error_label)) },
            syncMemberEnrollmentRecordUseCase.execute { setError(it, getString(R.string.sync_member_enrollment_records_error_label)) },
            syncMembershipPaymentUseCase.execute { setError(it, getString(R.string.sync_payments_error_label)) },
            Completable.fromAction {
                if (getErrorMessages().isEmpty()) {
                    preferencesManager.updateDataLastSynced(clock.instant())
                }
            }
        )
    }
}

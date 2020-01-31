package org.watsi.enrollment.services

import io.reactivex.Completable
import org.threeten.bp.Clock
import org.watsi.enrollment.R
import org.watsi.enrollment.device.managers.PreferencesManager
import org.watsi.enrollment.domain.usecases.FetchAdministrativeDivisionsUseCase
import org.watsi.enrollment.domain.usecases.FetchEnrollmentPeriodsUseCase
import org.watsi.enrollment.domain.usecases.FetchHouseholdsUseCase
import org.watsi.enrollment.domain.usecases.FetchMemberPhotosUseCase
import javax.inject.Inject

class FetchService : BaseService() {

    @Inject lateinit var fetchAdministrativeDivisionsUseCase: FetchAdministrativeDivisionsUseCase
    @Inject lateinit var fetchEnrollmentPeriodsUseCase: FetchEnrollmentPeriodsUseCase
    @Inject lateinit var fetchHouseholdsUseCase: FetchHouseholdsUseCase
    @Inject lateinit var fetchMemberPhotosUseCase: FetchMemberPhotosUseCase
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var clock: Clock

    override fun executeTasks(): Completable {
        return Completable.concatArray(
            fetchAdministrativeDivisionsUseCase.execute().onErrorComplete { setError(it, getString(R.string.fetch_admin_divisions_error_label)) },
            fetchEnrollmentPeriodsUseCase.execute().onErrorComplete { setError(it, getString(R.string.fetch_enrollment_periods_error_label)) },
            fetchHouseholdsUseCase.execute().onErrorComplete { setError(it, getString(R.string.fetch_households_error_label)) },
            fetchMemberPhotosUseCase.execute().onErrorComplete { setError(it, getString(R.string.fetch_member_photos_error_label)) },
            Completable.fromAction {
                if (getErrorMessages().isEmpty()) {
                    preferencesManager.updateLastFetched(clock.instant())
                }
            }
        )
    }
}

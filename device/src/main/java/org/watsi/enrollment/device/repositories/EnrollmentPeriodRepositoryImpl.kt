package org.watsi.enrollment.device.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.enrollment.device.api.CoverageApi
import org.watsi.enrollment.device.db.daos.EnrollmentPeriodDao
import org.watsi.enrollment.device.db.models.EnrollmentPeriodModel
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.entities.EnrollmentPeriod
import org.watsi.enrollment.domain.repositories.EnrollmentPeriodRepository

class EnrollmentPeriodRepositoryImpl(
    private val enrollmentPeriodDao: EnrollmentPeriodDao,
    private val api: CoverageApi,
    private val sessionManager: SessionManager,
    private val okHttpClient: OkHttpClient,
    private val clock: Clock
): EnrollmentPeriodRepository {

    override fun current(): Flowable<EnrollmentPeriod> {
        return enrollmentPeriodDao.current(LocalDate.now(clock))
                .map { it.first().toEnrollmentPeriod() }
                .subscribeOn(Schedulers.io())
    }

    override fun fetch(): Completable {
        return Completable.fromAction {
            sessionManager.currentAuthenticationToken()?.let { token ->
                val enrollmentPeriods = api.getEnrollmentPeriods(token.getHeaderString())
                        .blockingGet().map { it.toEnrollmentPeriod() }

                val enrollmentPeriodModels = enrollmentPeriods.map {
                    EnrollmentPeriodModel.fromEnrollmentPeriod(it, clock)
                }

                enrollmentPeriodDao.upsert(enrollmentPeriodModels)
            } ?: Completable.error(Exception("Current token is null while calling EnrollmentPeriodRepositoryImpl.fetch"))
        }.subscribeOn(Schedulers.io())
    }

    override fun deleteAll(): Completable {
        return Completable.fromAction {
            okHttpClient.cache()?.evictAll()
            enrollmentPeriodDao.deleteAll()
        }.subscribeOn(Schedulers.io())
    }
}

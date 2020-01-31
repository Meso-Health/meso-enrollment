package org.watsi.enrollment.device.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.threeten.bp.Clock
import org.watsi.enrollment.device.api.CoverageApi
import org.watsi.enrollment.device.db.daos.AdministrativeDivisionDao
import org.watsi.enrollment.device.db.models.AdministrativeDivisionModel
import org.watsi.enrollment.device.managers.PreferencesManager
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.entities.AdministrativeDivision
import org.watsi.enrollment.domain.repositories.AdministrativeDivisionRepository

class AdministrativeDivisionRepositoryImpl(
    private val administrativeDivisionDao: AdministrativeDivisionDao,
    private val api: CoverageApi,
    private val sessionManager: SessionManager,
    private val clock: Clock,
    private val preferencesManager: PreferencesManager,
    private val okHttpClient: OkHttpClient
): AdministrativeDivisionRepository {

    override fun allWithLevel(level: String): Flowable<List<AdministrativeDivision>> {
        return administrativeDivisionDao.allWithLevel(level).map { models ->
            models.map { it.toAdministrativeDivision() }
        }.subscribeOn(Schedulers.io())
    }

    override fun fetch(): Completable {
        return Completable.fromAction {
            sessionManager.currentAuthenticationToken()?.let { token ->
                val divisionsWithinJurisdiction = api.getAdministrationDivisions(token.getHeaderString()).blockingGet()
                val models = divisionsWithinJurisdiction.map {
                    AdministrativeDivisionModel.fromAdministrativeDivision(it.toAdministrativeDivision(), clock)
                }

                // TODO: should also remove any divisions that are no longer applicable
                administrativeDivisionDao.upsert(models)
                preferencesManager.updateAdministrativeDivisionsLastFetched(clock.instant())
            } ?: Completable.error(Exception("Current token is null while calling AdministrativeDivisionRepositoryImpl.fetch"))
        }.subscribeOn(Schedulers.io())
    }

    override fun deleteAll(): Completable {
        return Completable.fromAction {
            okHttpClient.cache()?.evictAll()
            administrativeDivisionDao.deleteAll()
        }.subscribeOn(Schedulers.io())
    }
}

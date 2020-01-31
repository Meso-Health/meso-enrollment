package org.watsi.enrollment.device.repositories

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.enrollment.device.api.AdministrativeDivisionApi
import org.watsi.enrollment.device.api.CoverageApi
import org.watsi.enrollment.device.db.daos.AdministrativeDivisionDao
import org.watsi.enrollment.device.db.models.AdministrativeDivisionModel
import org.watsi.enrollment.device.managers.PreferencesManager
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.entities.AuthenticationToken
import org.watsi.enrollment.domain.factories.AdministrativeDivisionFactory
import org.watsi.enrollment.domain.factories.UserFactory

@RunWith(MockitoJUnitRunner::class)
class AdministrativeDivisionsRepositoryImplTest {

    @Mock lateinit var mockDao: AdministrativeDivisionDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    @Mock lateinit var mockPreferencesManager: PreferencesManager
    @Mock lateinit var mockOkHttpClient: OkHttpClient

    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    val parentAdminDivision = AdministrativeDivisionFactory.build()
    val user = UserFactory.build(administrativeDivisionId = parentAdminDivision.id)
    val token = AuthenticationToken("token", clock.instant(), user)
    lateinit var repo: AdministrativeDivisionRepositoryImpl

    @Before
    fun setup() {
        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        repo = AdministrativeDivisionRepositoryImpl(
            mockDao, mockApi, mockSessionManager, clock, mockPreferencesManager, mockOkHttpClient)
    }

    @Test
    fun fetch() {
        val childAdminDivision1 = AdministrativeDivisionFactory.build(parentId = parentAdminDivision.id)
        val childAdminDivision2 = AdministrativeDivisionFactory.build(parentId = parentAdminDivision.id)
        val allDivisons = listOf(parentAdminDivision, childAdminDivision1, childAdminDivision2)

        whenever(mockApi.getAdministrationDivisions(token.getHeaderString())).thenReturn(
            Single.just(allDivisons.map { AdministrativeDivisionApi(it) }))

        repo.fetch().test().assertComplete()
        verify(mockDao).upsert(allDivisons.map { AdministrativeDivisionModel.fromAdministrativeDivision(it, clock) })
        verify(mockPreferencesManager).updateAdministrativeDivisionsLastFetched(clock.instant())
    }
}

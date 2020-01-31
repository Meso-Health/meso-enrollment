package org.watsi.enrollment.device.managers

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.enrollment.device.api.AuthenticationTokenApi
import org.watsi.enrollment.device.api.CoverageApi
import org.watsi.enrollment.device.api.UserApi
import org.watsi.enrollment.domain.factories.AuthenticationTokenFactory
import org.watsi.enrollment.domain.factories.UserFactory

@RunWith(MockitoJUnitRunner::class)
class SessionManagerImplTest {

    @Mock lateinit var mockPreferencesManager: PreferencesManager
    @Mock lateinit var mockCoverageApi: CoverageApi
    @Mock lateinit var mockLogger: Logger
    lateinit var sessionManager: SessionManagerImpl

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        sessionManager = SessionManagerImpl(mockPreferencesManager, mockCoverageApi, mockLogger)
    }

    @Test
    fun login_allowedRole() {
        val username = "foo"
        val password = "bar"
        val token = AuthenticationTokenFactory.build(user = UserFactory.build(role = "enrollment"))
        val authenticationTokenApi = AuthenticationTokenApi(
            token = token.token,
            expiresAt = token.expiresAt.toString(),
            user = UserApi(token.user)
        )
        whenever(mockCoverageApi.login(any())).thenReturn(Single.just(authenticationTokenApi))

        val result = sessionManager.login(username, password).test()
        result.assertComplete()

        val parsedToken = authenticationTokenApi.toAuthenticationToken()
        verify(mockPreferencesManager).setAuthenticationToken(parsedToken)
        verify(mockLogger).setUser(parsedToken.user)
        assertEquals(sessionManager.currentAuthenticationToken(), parsedToken)
    }

    @Test
    fun login_admin() {
        val username = "foo"
        val password = "bar"
        val disallowedRoles = listOf("admin", "provider")
        disallowedRoles.forEach { disallowedRole ->
            val token = AuthenticationTokenFactory.build(user = UserFactory.build(role = disallowedRole))
            val authenticationTokenApi = AuthenticationTokenApi(
                token.token, token.expiresAt.toString(), UserApi(token.user))
            whenever(mockCoverageApi.login(any())).thenReturn(Single.just(authenticationTokenApi))

            val result = sessionManager.login(username, password).test()

            result.assertError(SessionManager.PermissionException::class.java)
        }
    }

    @Test
    fun logout() {
        sessionManager.logout()

        com.nhaarman.mockito_kotlin.verify(mockPreferencesManager).setAuthenticationToken(null)
        com.nhaarman.mockito_kotlin.verify(mockLogger).clearUser()
        assertNull(sessionManager.currentAuthenticationToken())
    }
}

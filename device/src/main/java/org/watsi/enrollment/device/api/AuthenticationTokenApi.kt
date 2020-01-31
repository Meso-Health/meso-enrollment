package org.watsi.enrollment.device.api

import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.AuthenticationToken

data class AuthenticationTokenApi(
    val token: String,
    val expiresAt: String,
    val user: UserApi
) {
    fun toAuthenticationToken(): AuthenticationToken {
        // have to transform expiresAt String to be compatible with Instant.parse
        val parsedExpiresAt = Instant.parse("${expiresAt.substring(0,23)}Z")
        return AuthenticationToken(token, parsedExpiresAt, user.toUser())
    }
}

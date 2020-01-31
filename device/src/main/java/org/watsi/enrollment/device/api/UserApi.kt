package org.watsi.enrollment.device.api

import org.watsi.enrollment.domain.entities.User

data class UserApi(
    val id: Int,
    val username: String,
    val name: String,
    val role: String,
    val administrativeDivisionId: Int
) {
    constructor(user: User): this(
        id = user.id,
        username = user.username,
        name = user.name,
        role = user.role,
        administrativeDivisionId = user.administrativeDivisionId
    )

    fun toUser(): User {
        return User(
            id = id,
            username = username,
            name = name,
            role = role,
            administrativeDivisionId = administrativeDivisionId
        )
    }
}

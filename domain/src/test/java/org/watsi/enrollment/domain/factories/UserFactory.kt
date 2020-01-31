package org.watsi.enrollment.domain.factories

import org.watsi.enrollment.domain.entities.User

object UserFactory {
    fun build(
        id: Int = 1,
        username: String = "foo",
        name: String = "Foo",
        role: String = "enrollment",
        administrativeDivisionId: Int = 1
    ): User {
        return User(
            id = id,
            username = username,
            name = name,
            role = role,
            administrativeDivisionId = administrativeDivisionId
        )
    }
}

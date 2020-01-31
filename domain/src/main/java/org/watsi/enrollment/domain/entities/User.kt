package org.watsi.enrollment.domain.entities

import java.io.Serializable

data class User(
    val id: Int,
    val username: String,
    val name: String,
    val role: String,
    val administrativeDivisionId: Int
): Serializable

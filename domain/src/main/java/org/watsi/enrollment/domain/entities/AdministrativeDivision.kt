package org.watsi.enrollment.domain.entities

import java.io.Serializable

data class AdministrativeDivision(
    val id: Int,
    val name: String,
    val level: String,
    val code: String?,
    val parentId: Int?
): Serializable

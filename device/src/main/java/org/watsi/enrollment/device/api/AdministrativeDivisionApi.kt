package org.watsi.enrollment.device.api

import org.watsi.enrollment.domain.entities.AdministrativeDivision

data class AdministrativeDivisionApi(
    val id: Int,
    val name: String,
    val level: String,
    val code: String?,
    val parentId: Int?
) {

    constructor (administrativeDivision: AdministrativeDivision) : this(
        id = administrativeDivision.id,
        name = administrativeDivision.name,
        level = administrativeDivision.level,
        code = administrativeDivision.code,
        parentId = administrativeDivision.parentId
    )

    fun toAdministrativeDivision(): AdministrativeDivision {
        return AdministrativeDivision(
            id = id,
            name = name,
            level = level,
            code = code,
            parentId = parentId
        )
    }
}

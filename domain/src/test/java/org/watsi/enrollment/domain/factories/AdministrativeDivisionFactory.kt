package org.watsi.enrollment.domain.factories

import org.watsi.enrollment.domain.entities.AdministrativeDivision

object AdministrativeDivisionFactory {
    fun build(
        id: Int = 1,
        name: String = "Village A",
        level: String = "village",
        code: String? = null,
        parentId: Int? = 10
    ): AdministrativeDivision {
        return AdministrativeDivision(
            id = id,
            name = name,
            level = level,
            code = code,
            parentId = parentId
        )
    }
}

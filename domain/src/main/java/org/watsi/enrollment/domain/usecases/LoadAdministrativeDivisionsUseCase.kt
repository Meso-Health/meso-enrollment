package org.watsi.enrollment.domain.usecases

import io.reactivex.Flowable
import org.watsi.enrollment.domain.entities.AdministrativeDivision
import org.watsi.enrollment.domain.repositories.AdministrativeDivisionRepository

class LoadAdministrativeDivisionsUseCase(
    private val administrativeDivisionRepository: AdministrativeDivisionRepository
) {

    fun execute(level: String): Flowable<List<AdministrativeDivision>> {
        return administrativeDivisionRepository.allWithLevel(level)
    }
}

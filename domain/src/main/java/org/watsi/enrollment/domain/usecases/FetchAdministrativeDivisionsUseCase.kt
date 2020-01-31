package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import org.watsi.enrollment.domain.repositories.AdministrativeDivisionRepository

class FetchAdministrativeDivisionsUseCase(
    private val administrativeDivisionRepository: AdministrativeDivisionRepository
) {

    fun execute(): Completable {
        return administrativeDivisionRepository.fetch()
    }
}

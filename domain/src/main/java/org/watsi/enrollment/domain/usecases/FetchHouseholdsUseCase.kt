package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import org.watsi.enrollment.domain.repositories.HouseholdRepository

class FetchHouseholdsUseCase(
    private val householdRepository: HouseholdRepository
) {
    fun execute(): Completable {
        return householdRepository.fetch()
    }
}

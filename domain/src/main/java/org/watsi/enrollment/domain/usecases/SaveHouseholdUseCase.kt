package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import org.watsi.enrollment.domain.entities.Household
import org.watsi.enrollment.domain.repositories.HouseholdRepository

class SaveHouseholdUseCase(private val householdRepository: HouseholdRepository) {

    fun execute(household: Household): Completable {
        return householdRepository.save(household)
    }
}

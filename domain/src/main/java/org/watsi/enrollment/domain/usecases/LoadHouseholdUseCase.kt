package org.watsi.enrollment.domain.usecases

import io.reactivex.Flowable
import org.watsi.enrollment.domain.relations.HouseholdWithMembersAndPayments
import org.watsi.enrollment.domain.repositories.HouseholdRepository
import java.util.UUID

class LoadHouseholdUseCase constructor (private val householdRepository: HouseholdRepository) {

    fun execute(householdId: UUID): Flowable<HouseholdWithMembersAndPayments> {
        return householdRepository.get(householdId)
    }
}

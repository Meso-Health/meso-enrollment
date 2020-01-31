package org.watsi.enrollment.domain.usecases

import io.reactivex.Flowable
import org.threeten.bp.Instant
import org.watsi.enrollment.domain.relations.HouseholdWithMembers
import org.watsi.enrollment.domain.repositories.HouseholdRepository

class LoadHouseholdsUseCase constructor (
    private val householdRepository: HouseholdRepository
) {
    fun execute(instant: Instant): Flowable<List<HouseholdWithMembers>> {
        return householdRepository.createdOrEditedAfter(instant)
    }
}

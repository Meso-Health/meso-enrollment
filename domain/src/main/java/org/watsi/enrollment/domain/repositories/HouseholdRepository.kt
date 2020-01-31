package org.watsi.enrollment.domain.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.Household
import org.watsi.enrollment.domain.relations.HouseholdWithMembers
import org.watsi.enrollment.domain.relations.HouseholdWithMembersAndPayments
import java.util.UUID

interface HouseholdRepository {
    fun createdOrEditedAfter(instant: Instant): Flowable<List<HouseholdWithMembers>>
    fun get(householdId: UUID): Flowable<HouseholdWithMembersAndPayments>
    fun save(household: Household): Completable
    fun fetch(): Completable
    fun sync(deltas: List<Delta>): Completable
    fun deleteAll(): Completable
}

package org.watsi.enrollment.domain.repositories

import io.reactivex.Completable
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.HouseholdEnrollmentRecord

interface HouseholdEnrollmentRecordRepository {
    fun save(householdEnrollmentRecord: HouseholdEnrollmentRecord, delta: Delta): Completable
    fun sync(deltas: List<Delta>): Completable
    fun deleteAll(): Completable
}

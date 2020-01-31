package org.watsi.enrollment.domain.repositories

import io.reactivex.Completable
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.MemberEnrollmentRecord

interface MemberEnrollmentRecordRepository {
    fun save(memberEnrollmentRecord: MemberEnrollmentRecord, delta: Delta): Completable
    fun sync(deltas: List<Delta>): Completable
    fun deleteAll(): Completable
}

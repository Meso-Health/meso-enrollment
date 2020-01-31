package org.watsi.enrollment.domain.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.MembershipPayment
import java.util.UUID

interface MembershipPaymentRepository {
    fun save(membershipPayment: MembershipPayment, delta: Delta): Completable
    fun sync(deltas: List<Delta>): Completable
    fun findByHousehold(householdId: UUID): Flowable<List<MembershipPayment>>
    fun totalCollected(): Flowable<Int>
    fun deleteAll(): Completable
}

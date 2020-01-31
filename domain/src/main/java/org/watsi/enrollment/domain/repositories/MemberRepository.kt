package org.watsi.enrollment.domain.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import java.util.UUID

interface MemberRepository {
    fun all(): Flowable<List<Member>>
    fun get(id: UUID): Single<Member>
    fun getFlowable(id: UUID): Flowable<MemberWithThumbnail>
    fun withCardId(cardId: String): Maybe<Member>
    fun withMembershipNumber(membershipNumber: String): Maybe<Member>
    fun create(member: Member, deltas: List<Delta>): Completable
    fun update(member: Member, deltas: List<Delta>): Completable
    fun sync(deltas: List<Delta>): Completable
    fun byIds(ids: List<UUID>): Single<List<MemberWithThumbnail>>
    fun byNames(names: List<String>): Single<List<MemberWithThumbnail>>
    fun byCardIds(names: List<String>): Single<List<MemberWithThumbnail>>
    fun downloadPhotos(): Completable
    fun withPhotosToFetchCount(): Flowable<Int>
    fun deleteAll(): Completable
    fun updateMembershipNumber(id: UUID, membershipNumber: String): Completable
    fun allDistinctNames(): Single<List<String>>
    fun allDistinctCardIds(): Single<List<String>>
}

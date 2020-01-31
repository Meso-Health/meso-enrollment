package org.watsi.enrollment.device.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.threeten.bp.Clock
import org.watsi.enrollment.device.api.CoverageApi
import org.watsi.enrollment.device.api.MemberApi
import org.watsi.enrollment.device.db.DbHelper
import org.watsi.enrollment.device.db.daos.MemberDao
import org.watsi.enrollment.device.db.daos.PhotoDao
import org.watsi.enrollment.device.db.models.DeltaModel
import org.watsi.enrollment.device.db.models.MemberModel
import org.watsi.enrollment.device.db.models.PhotoModel
import org.watsi.enrollment.device.managers.PreferencesManager
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.Photo
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import org.watsi.enrollment.domain.repositories.MemberRepository
import java.util.UUID

class MemberRepositoryImpl(
        private val memberDao: MemberDao,
        private val photoDao: PhotoDao,
        private val api: CoverageApi,
        private val sessionManager: SessionManager,
        private val clock: Clock,
        private val okHttpClient: OkHttpClient,
        private val preferencesManager: PreferencesManager
) : MemberRepository {

    override fun all(): Flowable<List<Member>> {
        return memberDao.all().map { it.map { it.toMember() } }.subscribeOn(Schedulers.io())
    }

    override fun withCardId(cardId: String): Maybe<Member> {
        return memberDao.withCardId(cardId).map { it.toMember() }
    }

    override fun withMembershipNumber(membershipNumber: String): Maybe<Member> {
        return memberDao.withMembershipNumber(membershipNumber).map { it.toMember() }.subscribeOn(Schedulers.io())
    }

    override fun get(id: UUID): Single<Member> {
        return memberDao.get(id).map { it.toMember() }.subscribeOn(Schedulers.io())
    }

    override fun getFlowable(id: UUID): Flowable<MemberWithThumbnail> {
        return memberDao.getFlowable(id).map { it.toMemberWithThumbnail() }
    }

    override fun create(member: Member, deltas: List<Delta>): Completable {
        return Completable.fromAction {
            val deltaModels = deltas.map { DeltaModel.fromDelta(it, clock) }
            memberDao.insertWithDeltas(MemberModel.fromMember(member, clock), deltaModels)
        }.subscribeOn(Schedulers.io())
    }

    override fun update(member: Member, deltas: List<Delta>): Completable {
        return Completable.fromAction {
            val deltaModels = deltas.map { DeltaModel.fromDelta(it, clock) }
            memberDao.updateWithDeltas(MemberModel.fromMember(member, clock), deltaModels)
        }.subscribeOn(Schedulers.io())
    }

    override fun sync(deltas: List<Delta>): Completable {
        return sessionManager.currentAuthenticationToken()?.let { authToken ->
            memberDao.get(deltas.first().modelId).flatMap {
                val member = it.toMember()
                if (deltas.any { it.action == Delta.Action.ADD }) {
                    api.postMember(authToken.getHeaderString(), MemberApi(member))
                } else {
                    api.patchMember(authToken.getHeaderString(), member.id, MemberApi.patch(member, deltas))
                }
            }.toCompletable().subscribeOn(Schedulers.io())
        } ?: Completable.error(Exception("Current token is null while calling MemberRepositoryImpl.sync"))
    }

    override fun byIds(ids: List<UUID>): Single<List<MemberWithThumbnail>> {
        return Single.fromCallable {
            ids.chunked(DbHelper.SQLITE_MAX_VARIABLE_NUMBER).map {
                memberDao.findMemberRelationsByIds(it).blockingGet()
            }.flatten().map { it.toMemberWithThumbnail() }
        }.subscribeOn(Schedulers.io())
    }

    override fun byNames(names: List<String>): Single<List<MemberWithThumbnail>> {
        return Single.fromCallable {
            names.chunked(DbHelper.SQLITE_MAX_VARIABLE_NUMBER).map {
                memberDao.findMemberRelationsByName(it).blockingGet()
            }.flatten().map { it.toMemberWithThumbnail() }
        }.subscribeOn(Schedulers.io())
    }

    override fun byCardIds(names: List<String>): Single<List<MemberWithThumbnail>> {
        return Single.fromCallable {
            names.chunked(DbHelper.SQLITE_MAX_VARIABLE_NUMBER).map {
                memberDao.findMemberRelationsByCardId(it).blockingGet()
            }.flatten().map { it.toMemberWithThumbnail() }
        }.subscribeOn(Schedulers.io())
    }

    override fun downloadPhotos(): Completable {
        return Completable.fromAction {
            val membersThatNeedPhotoDownload = memberDao.needPhotoDownload().blockingGet()

            membersThatNeedPhotoDownload.map { memberModel ->
                val response = api.fetchPhoto(memberModel.photoUrl!!).blockingGet()
                val photo = Photo(UUID.randomUUID(), response.bytes())
                photoDao.insert(PhotoModel.fromPhoto(photo, clock))
                memberDao.upsert(memberModel.copy(thumbnailPhotoId = photo.id))
            }

            preferencesManager.updateMemberPhotosLastFetched(clock.instant())
        }.subscribeOn(Schedulers.io())
    }

    override fun withPhotosToFetchCount(): Flowable<Int> {
        return memberDao.needPhotoDownloadCount()
    }

    override fun deleteAll(): Completable {
        return Completable.fromAction {
            okHttpClient.cache()?.evictAll()
            memberDao.deleteAll()
        }.subscribeOn(Schedulers.io())
    }

    override fun updateMembershipNumber(id: UUID, membershipNumber: String): Completable {
        return Completable.fromAction {
            memberDao.updateMembershipNumber(id, membershipNumber)
        }
    }

    override fun allDistinctCardIds(): Single<List<String>> {
        return memberDao.allDistinctCardIds().subscribeOn(Schedulers.io())
    }

    override fun allDistinctNames(): Single<List<String>> {
        return memberDao.allDistinctNames().subscribeOn(Schedulers.io())
    }
}

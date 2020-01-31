package org.watsi.enrollment.device.repositories

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.threeten.bp.Clock
import org.watsi.enrollment.device.api.CoverageApi
import org.watsi.enrollment.device.api.MemberEnrollmentRecordApi
import org.watsi.enrollment.device.db.daos.MemberEnrollmentRecordDao
import org.watsi.enrollment.device.db.models.DeltaModel
import org.watsi.enrollment.device.db.models.MemberEnrollmentRecordModel
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.MemberEnrollmentRecord
import org.watsi.enrollment.domain.repositories.MemberEnrollmentRecordRepository
import org.watsi.enrollment.domain.repositories.MemberRepository

class MemberEnrollmentRecordRepositoryImpl constructor(
    private val memberEnrollmentRecordDao: MemberEnrollmentRecordDao,
    private val api: CoverageApi,
    private val clock: Clock,
    private val sessionManager: SessionManager,
    private val okHttpClient: OkHttpClient,
    private val memberRepository: MemberRepository
) : MemberEnrollmentRecordRepository {

    override fun sync(deltas: List<Delta>): Completable {
        return Completable.fromAction {
            sessionManager.currentAuthenticationToken()?.let { authToken ->
                val memberEnrollmentRecordModel = memberEnrollmentRecordDao.get(deltas.first().modelId).blockingGet()
                val memberEnrollmentRecord = memberEnrollmentRecordModel.toMemberEnrollmentRecord()
                if (deltas.any { it.action == Delta.Action.ADD }) {
                    val response = api.postMemberEnrollmentRecord(
                            authorization = authToken.getHeaderString(),
                            memberEnrollmentRecord = MemberEnrollmentRecordApi(memberEnrollmentRecord)
                    ).blockingGet()
                    response.membershipNumber?.let {
                        memberRepository.updateMembershipNumber(response.memberId, response.membershipNumber).blockingAwait()
                    }
                } else {
                    throw IllegalStateException("Delta with #{it.action} action not supported for MemberEnrollmentRecord")
                }
            } ?: Completable.error(Exception("Current token is null while calling MemberEnrollmentRecordRepositoryImpl.sync"))
        }.subscribeOn(Schedulers.io())
    }

    override fun save(memberEnrollmentRecord: MemberEnrollmentRecord, delta: Delta): Completable {
        return Completable.fromAction {
            memberEnrollmentRecordDao.insert(
                MemberEnrollmentRecordModel.fromMemberEnrollmentRecord(memberEnrollmentRecord, clock),
                DeltaModel.fromDelta(delta, clock)
            )
        }.subscribeOn(Schedulers.io())
    }

    override fun deleteAll(): Completable {
        return Completable.fromAction {
            okHttpClient.cache()?.evictAll()
            memberEnrollmentRecordDao.deleteAll()
        }.subscribeOn(Schedulers.io())
    }
}

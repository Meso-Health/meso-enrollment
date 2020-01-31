package org.watsi.enrollment.device.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.threeten.bp.Clock
import org.watsi.enrollment.device.api.CoverageApi
import org.watsi.enrollment.device.api.MembershipPaymentApi
import org.watsi.enrollment.device.db.daos.MembershipPaymentDao
import org.watsi.enrollment.device.db.models.DeltaModel
import org.watsi.enrollment.device.db.models.MembershipPaymentModel
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.MembershipPayment
import org.watsi.enrollment.domain.repositories.MembershipPaymentRepository
import java.util.UUID

class MembershipPaymentRepositoryImpl constructor(
    private val membershipPaymentDao: MembershipPaymentDao,
    private val api: CoverageApi,
    private val clock: Clock,
    private val sessionManager: SessionManager,
    private val okHttpClient: OkHttpClient
) : MembershipPaymentRepository {

    override fun save(membershipPayment: MembershipPayment, delta: Delta): Completable {
        return Completable.fromAction {
            membershipPaymentDao.insertWithDelta(
                membershipPaymentModel = MembershipPaymentModel.fromMembershipPayment(membershipPayment, clock),
                deltas = listOf(DeltaModel.fromDelta(delta, clock))
            )
        }.subscribeOn(Schedulers.io())
    }

    override fun findByHousehold(householdId: UUID): Flowable<List<MembershipPayment>> {
        return membershipPaymentDao.findByHousehold(householdId)
                .map { it.map { it.toMembershipPayment() } }
                .subscribeOn(Schedulers.io())
    }

    override fun sync(deltas: List<Delta>): Completable {
        return sessionManager.currentAuthenticationToken()?.let { authToken ->
            membershipPaymentDao.get(deltas.first().modelId).flatMap {
                val membershipPayment = it.toMembershipPayment()
                if (deltas.any { it.action == Delta.Action.ADD }) {
                    api.postMembershipPayment(authToken.getHeaderString(), MembershipPaymentApi(membershipPayment))
                } else {
                    throw IllegalStateException("Delta with #{it.action} action not supported for MembershipPayment")
                }
            }.toCompletable().subscribeOn(Schedulers.io())
        } ?: Completable.error(Exception("Current token is null while calling MembershipPaymentRepositoryImpl.sync"))
    }

    override fun totalCollected(): Flowable<Int> {
        return membershipPaymentDao.totalCollected().subscribeOn(Schedulers.io())
    }


    override fun deleteAll(): Completable {
        return Completable.fromAction {
            okHttpClient.cache()?.evictAll()
            membershipPaymentDao.deleteAll()
        }.subscribeOn(Schedulers.io())
    }
}

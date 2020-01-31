package org.watsi.enrollment.device.repositories

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.enrollment.device.db.daos.DeltaDao
import org.watsi.enrollment.device.db.models.DeltaModel
import org.watsi.enrollment.device.factories.DeltaModelFactory
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.factories.DeltaFactory
import org.watsi.enrollment.domain.repositories.DeltaRepository

@RunWith(MockitoJUnitRunner::class)
class DeltaRepositoryImplTest {

    @Mock lateinit var mockDeltaDao: DeltaDao
    @Mock lateinit var okHttpClient: OkHttpClient
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    lateinit var deltaRepo: DeltaRepositoryImpl

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        deltaRepo = DeltaRepositoryImpl(mockDeltaDao, clock, okHttpClient)
    }

    @Test
    fun unsynced() {
        val modelName = Delta.ModelName.HOUSEHOLD
        val deltaModel = DeltaModelFactory.build(modelName = modelName)
        whenever(mockDeltaDao.unsynced(modelName)).thenReturn(Single.just(listOf(deltaModel)))

        deltaRepo.unsynced(modelName).test().assertValue(listOf(deltaModel.toDelta()))
    }

    @Test
    fun markAsSynced() {
        val delta = DeltaFactory.build(synced = false)

        deltaRepo.markAsSynced(listOf(delta)).test().assertComplete()

        verify(mockDeltaDao).update(listOf(DeltaModel.fromDelta(delta.copy(synced = true), clock)))
    }

    @Test
    fun syncStatus() {
        whenever(mockDeltaDao.countUnsynced(Delta.ModelName.HOUSEHOLD)).thenReturn(Flowable.just(2))
        whenever(mockDeltaDao.countUnsynced(Delta.ModelName.HOUSEHOLD_ENROLLMENT_RECORD)).thenReturn(Flowable.just(10))
        whenever(mockDeltaDao.countUnsynced(Delta.ModelName.MEMBER)).thenReturn(Flowable.just(3))
        whenever(mockDeltaDao.countUnsynced(Delta.ModelName.MEMBER_ENROLLMENT_RECORD)).thenReturn(Flowable.just(3))
        whenever(mockDeltaDao.countUnsynced(Delta.ModelName.PHOTO)).thenReturn(Flowable.just(1))
        whenever(mockDeltaDao.countUnsynced(Delta.ModelName.MEMBERSHIP_PAYMENT)).thenReturn(Flowable.just(5))

        val expected = DeltaRepository.SyncStatus(2, 10, 3, 3, 1, 5)

        deltaRepo.syncStatus().test().assertValue(expected)
    }
}

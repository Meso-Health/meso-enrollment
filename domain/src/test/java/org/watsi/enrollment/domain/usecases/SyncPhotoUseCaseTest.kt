package org.watsi.enrollment.domain.usecases

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.factories.DeltaFactory
import org.watsi.enrollment.domain.factories.MemberFactory
import org.watsi.enrollment.domain.repositories.DeltaRepository
import org.watsi.enrollment.domain.repositories.PhotoRepository

@RunWith(MockitoJUnitRunner::class)
class SyncPhotoUseCaseTest {

    @Mock lateinit var photoRepo: PhotoRepository
    @Mock lateinit var deltaRepo: DeltaRepository
    lateinit var useCase: SyncPhotoUseCase
    @Mock
    lateinit var onErrorCallback: (throwable: Throwable) -> Boolean

    val exception = Exception("Oh no there is an error")
    val syncedMember = MemberFactory.build()
    val syncedMemberDelta = DeltaFactory.build(action = Delta.Action.EDIT,
        modelName = Delta.ModelName.MEMBER,
        modelId = syncedMember.id,
        synced = false)
    val unsyncedMember = MemberFactory.build()
    val unsyncedMemberDelta = DeltaFactory.build(action = Delta.Action.ADD,
        modelName = Delta.ModelName.MEMBER,
        modelId = unsyncedMember.id,
        synced = false)
    val shouldBeSyncedPhotoDelta = DeltaFactory.build(
        action = Delta.Action.ADD,
        modelName = Delta.ModelName.PHOTO,
        modelId = syncedMember.id,
        synced = false)
    val shouldNotBeSyncedPhotoDelta = DeltaFactory.build(
        action = Delta.Action.ADD,
        modelName = Delta.ModelName.PHOTO,
        modelId = unsyncedMember.id,
        synced = false)

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        useCase = SyncPhotoUseCase(photoRepo, deltaRepo)
        whenever(onErrorCallback.invoke(any())).thenReturn(true)
        whenever(deltaRepo.unsynced(Delta.ModelName.MEMBER))
                .thenReturn(Single.just(listOf(syncedMemberDelta, unsyncedMemberDelta)))
        whenever(deltaRepo.unsynced(Delta.ModelName.PHOTO))
                .thenReturn(Single.just(listOf(shouldBeSyncedPhotoDelta, shouldNotBeSyncedPhotoDelta)))
        whenever(deltaRepo.markAsSynced(listOf(shouldBeSyncedPhotoDelta)))
                .thenReturn(Completable.complete())
    }

    @Test
    fun execute_success() {
        whenever(photoRepo.sync(listOf(shouldBeSyncedPhotoDelta))).thenReturn(Completable.complete())
        useCase.execute(onErrorCallback).test().assertComplete()
        verify(onErrorCallback, never()).invoke(any())
        verify(photoRepo, times(1)).sync(listOf(shouldBeSyncedPhotoDelta))
        verify(deltaRepo, times(1)).markAsSynced(listOf(shouldBeSyncedPhotoDelta))
    }

    @Test
    fun execute_onError() {
        whenever(photoRepo.sync(listOf(shouldBeSyncedPhotoDelta))).thenReturn(Completable.error(exception))
        useCase.execute(onErrorCallback).test().assertComplete()
        verify(onErrorCallback, times(1)).invoke(exception)
        verify(deltaRepo, never()).markAsSynced(any())
    }
}

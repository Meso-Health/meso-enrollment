package org.watsi.enrollment.domain.usecases

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
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.repositories.MemberRepository
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class UpdateMemberUseCaseTest {

    @Mock lateinit var mockMemberRepository: MemberRepository
    @Mock lateinit var mockMember: Member
    @Mock lateinit var mockPrevMember: Member
    val memberId = UUID.randomUUID()
    lateinit var useCase: UpdateMemberUseCase

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        useCase = UpdateMemberUseCase(mockMemberRepository)
        whenever(mockMember.id).thenReturn(memberId)
        whenever(mockMemberRepository.get(memberId)).thenReturn(Single.just(mockPrevMember))
    }

    @Test
    fun execute_photoIdDidNotChange_addsDiffDeltas() {
        val deltas = listOf("name", "gender", "language").map {
            Delta(action = Delta.Action.EDIT,
                  modelName = Delta.ModelName.MEMBER,
                  modelId = memberId)
        }
        whenever(mockMember.diff(mockPrevMember)).thenReturn(deltas)
        whenever(mockMemberRepository.update(mockMember, deltas))
                .thenReturn(Completable.complete())

        useCase.execute(mockMember).test().assertComplete()
    }

    @Test
    fun execute_photoIdChanged_addsDiffDeltasAndPhotoAddDelta() {
        val deltas = listOf("name", "gender", "photoId").map {
            Delta(action = Delta.Action.EDIT,
                    modelName = Delta.ModelName.MEMBER,
                    modelId = memberId)
        }.toMutableList()
        deltas.add(Delta(action = Delta.Action.ADD,
                         modelName = Delta.ModelName.PHOTO,
                         modelId = memberId))
        whenever(mockMember.diff(mockPrevMember)).thenReturn(deltas)
        whenever(mockMemberRepository.update(mockMember, deltas))
                .thenReturn(Completable.complete())

        useCase.execute(mockMember).test().assertComplete()
    }
}

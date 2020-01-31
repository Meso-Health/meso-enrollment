package org.watsi.enrollment.domain.usecases

import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.factories.MemberFactory
import org.watsi.enrollment.domain.repositories.MemberRepository

@RunWith(MockitoJUnitRunner::class)
class ArchiveMemberUseCaseTest {

    @Mock lateinit var mockMemberRepository: MemberRepository
    lateinit var useCase: ArchiveMemberUseCase

    @Before
    fun setup() {
        useCase = ArchiveMemberUseCase(mockMemberRepository)
    }

    @Test
    fun execute() {
        val instant = Instant.now()
        val clock = Clock.fixed(instant, ZoneId.systemDefault())
        val member = MemberFactory.build()
        val deltas = listOf(
            Delta(
                action = Delta.Action.EDIT,
                modelName = Delta.ModelName.MEMBER,
                modelId = member.id,
                field = "archivedAt"
            ),
            Delta(
                action = Delta.Action.EDIT,
                modelName = Delta.ModelName.MEMBER,
                modelId = member.id,
                field = "archivedReason"
            )
        )
        val archivedReason = "relocation"

        useCase.execute(member, archivedReason, clock)

        val archivedMember = member.copy(archivedReason = archivedReason, archivedAt = instant)
        verify(mockMemberRepository).update(archivedMember, deltas)
    }
}

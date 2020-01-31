package org.watsi.enrollment.domain.usecases

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import org.watsi.enrollment.domain.repositories.MemberRepository
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class LoadMemberUseCaseTest {

    @Mock lateinit var mockMemberRepository: MemberRepository
    @Mock lateinit var mockMemberWithThumbnailMaybe: Flowable<MemberWithThumbnail>
    lateinit var useCase: LoadMemberUseCase

    @Before
    fun setup() {
        useCase = LoadMemberUseCase(mockMemberRepository)
    }

    @Test
    fun execute() {
        val id = UUID.randomUUID()
        whenever(mockMemberRepository.getFlowable(id)).thenReturn(mockMemberWithThumbnailMaybe)

        assertEquals(useCase.execute(id), mockMemberWithThumbnailMaybe)
    }
}

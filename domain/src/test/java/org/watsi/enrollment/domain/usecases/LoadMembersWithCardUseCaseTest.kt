package org.watsi.enrollment.domain.usecases

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Maybe
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.repositories.MemberRepository

@RunWith(MockitoJUnitRunner::class)
class LoadMembersWithCardUseCaseTest {
    @Mock lateinit var mockMemberRepository: MemberRepository
    @Mock lateinit var mockMemberMaybe: Maybe<Member>
    lateinit var useCase: LoadMembersWithCardUseCase

    @Before
    fun setup() {
        useCase = LoadMembersWithCardUseCase(mockMemberRepository)
    }

    @Test
    fun execute() {
        val cardId = "RWI102939"
        useCase.execute(cardId)
        whenever(mockMemberRepository.withCardId(cardId)).thenReturn(mockMemberMaybe)
        assertEquals(useCase.execute(cardId), mockMemberMaybe)
    }
}

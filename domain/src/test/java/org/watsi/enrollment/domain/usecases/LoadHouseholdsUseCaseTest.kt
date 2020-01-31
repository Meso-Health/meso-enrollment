package org.watsi.enrollment.domain.usecases

import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Instant
import org.watsi.enrollment.domain.repositories.HouseholdRepository

@RunWith(MockitoJUnitRunner::class)
class LoadHouseholdsUseCaseTest {
    @Mock lateinit var mockHouseholdRepository: HouseholdRepository
    lateinit var loadHouseholdsUseCase: LoadHouseholdsUseCase

    @Before
    fun setup() {
        loadHouseholdsUseCase = LoadHouseholdsUseCase(mockHouseholdRepository)
    }

    @Test
    fun execute() {
        val now = Instant.now()
        loadHouseholdsUseCase.execute(now)
        verify(mockHouseholdRepository).createdOrEditedAfter(now)
    }
}

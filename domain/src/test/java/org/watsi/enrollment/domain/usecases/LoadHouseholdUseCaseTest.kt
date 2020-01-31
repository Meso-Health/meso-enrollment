package org.watsi.enrollment.domain.usecases

import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.enrollment.domain.repositories.HouseholdRepository
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class LoadHouseholdUseCaseTest {

    @Mock lateinit var mockHouseholdRepository: HouseholdRepository
    lateinit var loadHouseholdUseCase: LoadHouseholdUseCase

    @Before
    fun setup() {
        loadHouseholdUseCase = LoadHouseholdUseCase(mockHouseholdRepository)
    }

    @Test
    fun execute() {
        val householdId = UUID.randomUUID()

        loadHouseholdUseCase.execute(householdId)
        verify(mockHouseholdRepository).get(householdId)
    }
}

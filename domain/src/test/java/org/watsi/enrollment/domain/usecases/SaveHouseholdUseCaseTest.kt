package org.watsi.enrollment.domain.usecases

import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.enrollment.domain.entities.Household
import org.watsi.enrollment.domain.repositories.HouseholdRepository

@RunWith(MockitoJUnitRunner::class)
class SaveHouseholdUseCaseTest {

    @Mock lateinit var mockHouseholdRepository: HouseholdRepository
    @Mock lateinit var mockHousehold: Household
    lateinit var useCase: SaveHouseholdUseCase

    @Before
    fun setup() {
        useCase = SaveHouseholdUseCase(mockHouseholdRepository)
    }

    @Test
    fun execute_success() {
        useCase.execute(mockHousehold)

        verify(mockHouseholdRepository).save(mockHousehold)
    }
}

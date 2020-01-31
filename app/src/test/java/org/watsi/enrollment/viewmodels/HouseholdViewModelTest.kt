package org.watsi.enrollment.viewmodels

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.factories.EnrollmentPeriodFactory
import org.watsi.enrollment.domain.factories.HouseholdWithMembersAndPaymentsFactory
import org.watsi.enrollment.domain.usecases.LoadCurrentEnrollmentPeriodUseCase
import org.watsi.enrollment.domain.usecases.LoadHouseholdUseCase
import org.watsi.enrollment.testutils.AACBaseTest
import java.util.UUID

class HouseholdViewModelTest : AACBaseTest() {
    @Mock lateinit var loadHouseholdUseCase: LoadHouseholdUseCase
    @Mock lateinit var enrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase
    @Mock lateinit var logger: Logger
    val enrollmentPeriod = EnrollmentPeriodFactory.build()

    lateinit var viewModel: HouseholdViewModel

    @Before
    fun setup() {
        whenever(enrollmentPeriodUseCase.executeFlowable()).thenReturn(Flowable.just(enrollmentPeriod))
    }

    @Test
    fun init_stillLoading() {
        whenever(loadHouseholdUseCase.execute(any())).thenReturn(Flowable.never())
        viewModel = HouseholdViewModel(loadHouseholdUseCase, enrollmentPeriodUseCase, logger)
        val observable = viewModel.getObservable(UUID.randomUUID())
        observable.observeForever {  }

        val householdViewState = observable.value
        assertNull(householdViewState)
        assertNull(householdViewState?.householdWithMembers)
    }

    @Test
    fun init_error() {
        val exception = Exception("Oman failed to load household :(")
        whenever(loadHouseholdUseCase.execute(any())).thenReturn(Flowable.error(exception))
        viewModel = HouseholdViewModel(loadHouseholdUseCase, enrollmentPeriodUseCase, logger)
        val observable = viewModel.getObservable(UUID.randomUUID())
        observable.observeForever {  }

        val householdViewState = observable.value
        assertNotNull(householdViewState)
        assertNull(householdViewState?.householdWithMembers)
        verify(logger).error(exception)
    }

    @Test
    fun init_success() {
        val householdWithMembers = HouseholdWithMembersAndPaymentsFactory.build()
        whenever(loadHouseholdUseCase.execute(any())).thenReturn(Flowable.just(householdWithMembers))
        viewModel = HouseholdViewModel(loadHouseholdUseCase, enrollmentPeriodUseCase, logger)
        val observable = viewModel.getObservable(UUID.randomUUID())
        observable.observeForever {  }

        val householdViewState = observable.value
        assertNotNull(householdViewState)
        assertEquals(householdViewState?.householdWithMembers, householdWithMembers)
    }
}

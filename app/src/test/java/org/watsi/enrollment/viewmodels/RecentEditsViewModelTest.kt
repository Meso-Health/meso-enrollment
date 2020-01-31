package org.watsi.enrollment.viewmodels

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.factories.HouseholdFactory
import org.watsi.enrollment.domain.factories.MemberFactory
import org.watsi.enrollment.domain.factories.MemberWithThumbnailFactory
import org.watsi.enrollment.domain.relations.HouseholdWithMembers
import org.watsi.enrollment.domain.usecases.LoadHouseholdsUseCase
import org.watsi.enrollment.testutils.AACBaseTest

class RecentEditsViewModelTest : AACBaseTest() {
    @Mock lateinit var mockLoadHouseholdsUseCase: LoadHouseholdsUseCase
    @Mock lateinit var logger: Logger
    lateinit var clock: Clock
    lateinit var viewModel: RecentEditsViewModel

    @Before
    fun setup() {
        clock = Clock.fixed(Instant.now(), ZoneId.of("Africa/Addis_Ababa"))
    }

    @Test
    fun getObservable_noError_noRecentHouseholds() {
        whenever(mockLoadHouseholdsUseCase.execute(any()))
                .thenReturn(Flowable.just(emptyList()))
        viewModel = RecentEditsViewModel(mockLoadHouseholdsUseCase, clock, logger)

        val observable = viewModel.getObservable()
        observable.observeForever {}

        val expected = RecentEditsViewModel.ViewState(
            households = emptyList(),
            error = null
        )
        assertEquals(expected, observable.value)
    }

    @Test
    fun getObservable_noError_updatesObservable_filtersDeletedMembersAndHouseholds() {
        val household1 = HouseholdFactory.build()
        val member1 = MemberWithThumbnailFactory.build(member = MemberFactory.build(householdId = household1.id))
        val member2 = MemberWithThumbnailFactory.build(member = MemberFactory.build(householdId = household1.id))
        val unpaidMember = MemberWithThumbnailFactory.build(member = MemberFactory.build(householdId = household1.id, archivedAt = Instant.now(), archivedReason = "unpaid"))
        val household2 = HouseholdFactory.build()
        val member3 = MemberWithThumbnailFactory.build(member = MemberFactory.build(householdId = household2.id))
        val archivedMember = MemberWithThumbnailFactory.build(member = MemberFactory.build(householdId = household2.id, archivedAt = Instant.now(), archivedReason = "death"))
        val household3 = HouseholdFactory.build()
        val member4deleted = MemberWithThumbnailFactory.build(member = MemberFactory.build(householdId = household3.id, archivedReason = "death"))
        val member5deleted = MemberWithThumbnailFactory.build(member = MemberFactory.build(householdId = household3.id, archivedReason = "other"))
        val household4 = HouseholdFactory.build()
        val member6deleted = MemberWithThumbnailFactory.build(member = MemberFactory.build(householdId = household4.id, archivedReason = "death"))
        val member7unpaid = MemberWithThumbnailFactory.build(member = MemberFactory.build(householdId = household4.id, archivedReason = "unpaid"))

        val householdWithMembersList = listOf(
            HouseholdWithMembers(household1, listOf(member1, member2, unpaidMember), null),
            HouseholdWithMembers(household2, listOf(member3, archivedMember), null),
            HouseholdWithMembers(household3, listOf(member4deleted, member5deleted), null),
            HouseholdWithMembers(household4, listOf(member6deleted, member7unpaid), null)
        )

        whenever(mockLoadHouseholdsUseCase.execute(any()))
            .thenReturn(Flowable.just(householdWithMembersList))
        viewModel = RecentEditsViewModel(mockLoadHouseholdsUseCase, clock, logger)

        val observable = viewModel.getObservable()
        observable.observeForever {}

        val filteredHouseholdWithMembersList = listOf(
            HouseholdWithMembers(household1, listOf(member1, member2, unpaidMember), null),
            HouseholdWithMembers(household2, listOf(member3), null),
            HouseholdWithMembers(household4, listOf(member7unpaid), null)
        )

        val expected = RecentEditsViewModel.ViewState(
            households = filteredHouseholdWithMembersList,
            error = null
        )
        assertEquals(expected, observable.value)
    }

    @Test
    fun getObservable_queryErrors_returnsErrorState() {
        val errorMessage = "Query failed"
        val exception = Exception(errorMessage)
        whenever(mockLoadHouseholdsUseCase.execute(any()))
                .thenReturn(Flowable.error(exception))
        viewModel = RecentEditsViewModel(mockLoadHouseholdsUseCase, clock, logger)

        val observable = viewModel.getObservable()
        observable.observeForever {}

        val expected = RecentEditsViewModel.ViewState(
            households = emptyList(),
            error = errorMessage
        )
        verify(logger, times(1)).error(exception)
        assertEquals(expected, observable.value)
    }
}

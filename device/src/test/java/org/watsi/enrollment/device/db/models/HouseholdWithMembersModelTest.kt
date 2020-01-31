package org.watsi.enrollment.device.db.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.watsi.enrollment.device.factories.AdministrativeDivisionModelFactory
import org.watsi.enrollment.device.factories.HouseholdModelFactory
import org.watsi.enrollment.device.factories.MemberModelFactory
import org.watsi.enrollment.device.factories.MemberWithThumbnailModelFactory

class HouseholdWithMembersModelTest {

    @Test(expected = IllegalStateException::class)
    fun toHouseholdWithMembers_nullHousehold_throwsException() {
        val administrativeDivisionModel = AdministrativeDivisionModelFactory.build()
        val householdModel = HouseholdModelFactory.build(administrativeDivisionId = administrativeDivisionModel.id)
        val memberModel = MemberModelFactory.build(householdId = householdModel.id)
        val memberWithThumbnailModel = MemberWithThumbnailModelFactory.build(memberModel)

        HouseholdWithMembersModel(null, listOf(memberWithThumbnailModel))
                .toHouseholdWithMembers()
    }

    @Test
    fun toHouseholdWithMembers_nullMembers_setsMemberAsEmptyList() {
        val administrativeDivisionModel = AdministrativeDivisionModelFactory.build()
        val householdModel = HouseholdModelFactory.build(administrativeDivisionId = administrativeDivisionModel.id)

        val relation = HouseholdWithMembersModel(householdModel, null)
                .toHouseholdWithMembers()

        assertEquals(householdModel.toHousehold(), relation.household)
        assertEquals(emptyList<MemberWithThumbnailModel>(), relation.members)
    }

    @Test
    fun toHouseholdWithMembers_withMembers_setsMembers() {
        val administrativeDivisionModel = AdministrativeDivisionModelFactory.build()
        val householdModel = HouseholdModelFactory.build(administrativeDivisionId = administrativeDivisionModel.id)
        val memberModel = MemberModelFactory.build(householdId = householdModel.id)
        val memberWithThumbnailModel = MemberWithThumbnailModelFactory.build(memberModel)

        val relation = HouseholdWithMembersModel(householdModel, listOf(memberWithThumbnailModel))
                .toHouseholdWithMembers()

        assertEquals(householdModel.toHousehold(), relation.household)
        assertEquals(listOf(memberWithThumbnailModel.toMemberWithThumbnail()), relation.members)
    }
}

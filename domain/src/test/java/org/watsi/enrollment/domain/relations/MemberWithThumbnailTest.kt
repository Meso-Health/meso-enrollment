package org.watsi.enrollment.domain.relations

import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.Instant
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.factories.HouseholdFactory
import org.watsi.enrollment.domain.factories.MemberFactory
import org.watsi.enrollment.domain.factories.MemberWithThumbnailFactory

class MemberWithThumbnailTest {

    @Test
    fun asSortedListWithHeadOfHouseholdsFirst() {
        val now = Instant.now()

        val household = HouseholdFactory.build()
        val member1 = MemberFactory.build(
            householdId = household.id,
            enrolledAt = now.plusSeconds(6000)
        )
        val member2 = MemberFactory.build(
            householdId = household.id,
            enrolledAt = now
        )
        val member3 = MemberFactory.build(
            householdId = household.id,
            enrolledAt = now.minusSeconds(6000)
        )
        val member4 = MemberFactory.build(
            householdId = household.id,
            relationshipToHead = Member.RELATIONSHIP_TO_HEAD_SELF,
            enrolledAt = now.plusSeconds(8000)
        )
        val member5 = MemberFactory.build(
            householdId = household.id,
            relationshipToHead = Member.RELATIONSHIP_TO_HEAD_SELF,
            enrolledAt = now.minusSeconds(12000)
        )
        val memberWithThumbnail1 = MemberWithThumbnailFactory.build(member = member1)
        val memberWithThumbnail2 = MemberWithThumbnailFactory.build(member = member2)
        val memberWithThumbnail3 = MemberWithThumbnailFactory.build(member = member3)
        val memberWithThumbnail4 = MemberWithThumbnailFactory.build(member = member4)
        val memberWithThumbnail5 = MemberWithThumbnailFactory.build(member = member5)

        val membersInWrongOrder = listOf(
            memberWithThumbnail1,
            memberWithThumbnail2,
            memberWithThumbnail3,
            memberWithThumbnail4,
            memberWithThumbnail5
        )

        val membersInRightOrder = listOf(
            memberWithThumbnail5,
            memberWithThumbnail4,
            memberWithThumbnail3,
            memberWithThumbnail2,
            memberWithThumbnail1
        )
        assertEquals(
            MemberWithThumbnail.asSortedListWithHeadOfHouseholdsFirst(membersInWrongOrder),
            membersInRightOrder
        )
    }
}

package org.watsi.enrollment.device.db.daos

import io.reactivex.Completable
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.watsi.enrollment.device.db.models.HouseholdModel
import org.watsi.enrollment.device.db.models.MemberWithThumbnailModel
import org.watsi.enrollment.device.factories.AdministrativeDivisionModelFactory
import org.watsi.enrollment.device.factories.DeltaModelFactory
import org.watsi.enrollment.device.factories.HouseholdModelFactory
import org.watsi.enrollment.device.factories.MemberModelFactory
import org.watsi.enrollment.device.factories.PhotoModelFactory
import java.util.UUID

class MemberDaoTest : DaoBaseTest() {
    lateinit var household: HouseholdModel

    @Before
    fun setUp() {
        val administrativeDivisionModel = AdministrativeDivisionModelFactory.create(administrativeDivisionDao)
        household = HouseholdModelFactory.create(householdDao, administrativeDivisionId = administrativeDivisionModel.id)
    }

    @Test
    fun withCardId() {
        val cardId = "RWI123123"
        val m1 = MemberModelFactory.create(memberDao, cardId = cardId, householdId = household.id)
        MemberModelFactory.create(memberDao, householdId = household.id)
        MemberModelFactory.create(memberDao, householdId = household.id)

        memberDao.withCardId(cardId).test().assertValue(m1)
        memberDao.withCardId("RANDOMCARDID").test().assertResult()
    }

    @Test
    fun findMemberRelationsByIds() {
        val photoModel = PhotoModelFactory.create(photoDao)
        val memberModel = MemberModelFactory.create(memberDao, thumbnailPhotoId = photoModel.id, householdId = household.id)

        val expectedRelationModel = MemberWithThumbnailModel(
            memberModel = memberModel,
            photoModels = listOf(photoModel)
        )

        memberDao.findMemberRelationsByIds(listOf(memberModel.id)).test().assertValue(listOf(expectedRelationModel))
    }

    @Test
    fun findMemberRelationsByNames() {
        val memberModel1 = MemberModelFactory.create(memberDao, name = "Buster H Posey", householdId = household.id)
        MemberModelFactory.create(memberDao, name = "Stephen Tikka Masala", householdId = household.id)
        val memberModel3 = MemberModelFactory.create(memberDao, name = "Klay Thompson Jackson", householdId = household.id)
        val memberModel4 = MemberModelFactory.create(memberDao, name = "Klay Thompson Jackson", householdId = household.id)

        val matchingMembers = memberDao.findMemberRelationsByName(
            names = listOf(
                "Buster H Posey",
                "Klay Thompson Jackson"
            )
        ).test().values().first()
        assertEquals(
            matchingMembers.map { it.memberModel?.id },
            listOf(memberModel1.id, memberModel3.id, memberModel4.id)
        )
    }

    @Test
    fun findMemberRelationsByCardId() {
        val memberModel1 = MemberModelFactory.create(memberDao, cardId = "ETH000000", householdId = household.id)
        MemberModelFactory.create(memberDao, cardId = "ETH111111", householdId = household.id)
        val memberModel3 = MemberModelFactory.create(memberDao, cardId = "ETH222222", householdId = household.id)
        val memberModel4 = MemberModelFactory.create(memberDao, cardId = "ETH333333", householdId = household.id)

        val matchingMembers = memberDao.findMemberRelationsByCardId(
            cardIds = listOf(
                "ETH000000",
                "ETH222222",
                "ETH333333"
            )
        ).test().values().first()
        assertEquals(
            matchingMembers.map { it.memberModel?.id },
            listOf(memberModel1.id, memberModel3.id, memberModel4.id)
        )
    }

    @Test
    fun updateWithDeltas() {
        val memberModel = MemberModelFactory.create(memberDao, householdId = household.id)
        val name = "Updated Name"
        val deltaModel = DeltaModelFactory.build()

        Completable.fromAction {
            memberDao.updateWithDeltas(memberModel.copy(name = name), listOf(deltaModel))
        }.test().assertComplete()

        val updatedModel = memberDao.get(memberModel.id).test().values().first()
        assertEquals(name, updatedModel.name)
        deltaDao.getAll()
    }

    @Test
    fun updateWithDeltas_runsWithinTransaction() {
        val memberModel = MemberModelFactory.create(memberDao, householdId = household.id)
        val name = "Updated Name"
        val deltaModel = DeltaModelFactory.build(id = 1)

        Completable.fromAction {
            memberDao.updateWithDeltas(memberModel.copy(name = name), listOf(deltaModel, deltaModel))
        }.test().assertError(Exception::class.java)

        deltaDao.getAll().test().assertValue(emptyList())
        val queriedModel = memberDao.get(memberModel.id).test().values().first()
        assertEquals(memberModel, queriedModel)
    }

    @Test
    fun needPhotoDownload() {
        val needsPhoto = MemberModelFactory.create(memberDao, photoUrl = "foo", thumbnailPhotoId = null, householdId = household.id)
        // photo downloaded
        MemberModelFactory.create(memberDao, photoUrl = "foo", thumbnailPhotoId = UUID.randomUUID(), householdId = household.id)
        // does not have photo
        MemberModelFactory.create(memberDao, photoUrl = null, householdId = household.id)

        memberDao.needPhotoDownload().test().assertValue(listOf(needsPhoto))
    }

    @Test
    fun needPhotoDownloadCount() {
        // awaiting photo download
        MemberModelFactory.create(memberDao, photoUrl = "foo", thumbnailPhotoId = null, householdId = household.id)
        // photo downloaded
        MemberModelFactory.create(memberDao, photoUrl = "foo", thumbnailPhotoId = UUID.randomUUID(), householdId = household.id)
        // does not have photo
        MemberModelFactory.create(memberDao, photoUrl = null, householdId = household.id)

        memberDao.needPhotoDownloadCount().test().assertValue(1)
    }

    @Test
    fun allDistinctNames() {
        MemberModelFactory.create(memberDao, name = "Buster H Posey", householdId = household.id)
        MemberModelFactory.create(memberDao, name = "Stephen Tikka Masala", householdId = household.id)
        MemberModelFactory.create(memberDao, name = "Klay Thompson Jackson", householdId = household.id)
        MemberModelFactory.create(memberDao, name = "Klay Thompson Jackson", householdId = household.id)

        val distinctNames = memberDao.allDistinctNames().test().values().first()
        assertEquals(
            distinctNames.sorted(),
            listOf(
                "Buster H Posey",
                "Stephen Tikka Masala",
                "Klay Thompson Jackson"
            ).sorted()
        )
    }

    @Test
    fun allDistinctCardIds() {
        MemberModelFactory.create(memberDao, cardId = "ETH000000", householdId = household.id)
        MemberModelFactory.create(memberDao, cardId = "ETH111111", householdId = household.id)
        MemberModelFactory.create(memberDao, cardId = "ETH222222", householdId = household.id)
        MemberModelFactory.create(memberDao, cardId = "ETH333333", householdId = household.id)

        val distinctNames = memberDao.allDistinctCardIds().test().values().first()
        assertEquals(
            distinctNames.sorted(),
            listOf(
                "ETH000000",
                "ETH111111",
                "ETH222222",
                "ETH333333"
            ).sorted()
        )
    }
}

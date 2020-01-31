package org.watsi.enrollment.device.db.daos

import org.junit.Test
import org.watsi.enrollment.device.factories.DeltaModelFactory
import org.watsi.enrollment.domain.entities.Delta
import java.util.UUID

class DeltaDaoTest : DaoBaseTest() {

    @Test
    fun countUnsynced() {
        //  unsynced household add
        DeltaModelFactory.create(deltaDao, action = Delta.Action.ADD,
                modelName = Delta.ModelName.HOUSEHOLD, synced = false)
        //  unsynced household edit
        DeltaModelFactory.create(deltaDao, action = Delta.Action.EDIT,
                modelName = Delta.ModelName.HOUSEHOLD, synced = false)
        // synced household add
        DeltaModelFactory.create(deltaDao, action = Delta.Action.ADD,
                modelName = Delta.ModelName.HOUSEHOLD, synced = true)
        // synced member add
        DeltaModelFactory.create(deltaDao, action = Delta.Action.ADD,
                modelName = Delta.ModelName.MEMBER, synced = true)

        val memberId = UUID.randomUUID()
        // unsynced member add
        DeltaModelFactory.create(deltaDao, action = Delta.Action.ADD,
                modelName = Delta.ModelName.MEMBER, synced = false, modelId = memberId)
        // unsynced member edit
        DeltaModelFactory.create(deltaDao, action = Delta.Action.EDIT,
                modelName = Delta.ModelName.MEMBER, synced = false, modelId = memberId)

        deltaDao.countUnsynced(Delta.ModelName.MEMBER).test().assertValue(1)
        deltaDao.countUnsynced(Delta.ModelName.HOUSEHOLD).test().assertValue(2)
    }
}

package org.watsi.enrollment.device.db.daos

import org.junit.Test
import org.watsi.enrollment.device.factories.PhotoModelFactory

class PhotoDaoTest : DaoBaseTest() {
    @Test
    fun get_photoExistsWithSuppliedId_returnsPhoto() {
        val photoModel = PhotoModelFactory.create(photoDao)

        photoDao.get(photoModel.id).test().assertValue(photoModel)
    }
}

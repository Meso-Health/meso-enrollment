package org.watsi.enrollment.device.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import org.threeten.bp.Clock
import org.watsi.enrollment.device.api.CoverageApi
import org.watsi.enrollment.device.db.daos.PhotoDao
import org.watsi.enrollment.device.db.models.PhotoModel
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.Photo
import org.watsi.enrollment.domain.repositories.PhotoRepository
import java.util.UUID

class PhotoRepositoryImpl constructor(
        private val photoDao: PhotoDao,
        private val api: CoverageApi,
        private val sessionManager: SessionManager,
        private val clock: Clock
) : PhotoRepository {

    override fun get(id: UUID): Single<Photo> {
        return photoDao.get(id).map { it.toPhoto() }.subscribeOn(Schedulers.io())
    }

    override fun insert(photo: Photo): Completable {
        return Completable.fromAction {
            photoDao.insert(PhotoModel.fromPhoto(photo, clock))
        }.subscribeOn(Schedulers.io())
    }

    override fun sync(deltas: List<Delta>): Completable {
        return sessionManager.currentAuthenticationToken()?.let { authToken ->
            val memberId = deltas.first().modelId

            // the modelId in a photo delta corresponds to the member ID and not the photo ID
            // to make this querying and formatting of the sync request simpler
            return photoDao.getMemberWithRawPhoto(memberId).flatMap { memberWithRawPhotoModel ->
                val memberWithRawPhoto = memberWithRawPhotoModel.toMemberWithRawPhoto()
                val requestBody = RequestBody.create(
                    MediaType.parse("image/jpg"), memberWithRawPhoto.photo.bytes)
                api.patchPhoto(authToken.getHeaderString(), memberId, requestBody)
            }.toCompletable()
        }
                ?: Completable.error(Exception("Current token is null while calling PhotoRepositoryImpl.sync"))
    }
}

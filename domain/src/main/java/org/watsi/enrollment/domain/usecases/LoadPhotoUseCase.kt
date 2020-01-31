package org.watsi.enrollment.domain.usecases

import io.reactivex.Single
import org.watsi.enrollment.domain.entities.Photo
import org.watsi.enrollment.domain.repositories.PhotoRepository
import java.util.UUID

class LoadPhotoUseCase(private val photoRepository: PhotoRepository) {

   fun execute(photoId: UUID): Single<Photo> {
       return photoRepository.get(photoId)
   }
}

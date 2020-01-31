package org.watsi.enrollment.domain.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.entities.Photo
import java.util.UUID

interface PhotoRepository {
    fun get(id: UUID): Single<Photo>
    fun insert(photo: Photo): Completable
    fun sync(deltas: List<Delta>): Completable
}

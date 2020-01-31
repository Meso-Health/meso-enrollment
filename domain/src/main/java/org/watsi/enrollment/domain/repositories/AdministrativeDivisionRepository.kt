package org.watsi.enrollment.domain.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import org.watsi.enrollment.domain.entities.AdministrativeDivision

interface AdministrativeDivisionRepository {
    fun allWithLevel(level: String): Flowable<List<AdministrativeDivision>>
    fun fetch(): Completable
    fun deleteAll(): Completable
}

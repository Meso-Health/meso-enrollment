package org.watsi.enrollment.domain.usecases

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.enrollment.domain.factories.PhotoFactory
import org.watsi.enrollment.domain.repositories.PhotoRepository

@RunWith(MockitoJUnitRunner::class)
class LoadPhotoUseCaseTest {
    @Mock lateinit var photoRepository: PhotoRepository
    lateinit var useCase: LoadPhotoUseCase

    @Before
    fun setup() {
        useCase = LoadPhotoUseCase(photoRepository)
    }

    @Test
    fun execute() {
        val photo = PhotoFactory.build()
        whenever(photoRepository.get(photo.id)).thenReturn(Single.just(photo))

        useCase.execute(photo.id).test().assertValue(photo)
    }
}

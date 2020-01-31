package org.watsi.enrollment.domain.usecases

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.enrollment.domain.entities.Photo
import org.watsi.enrollment.domain.repositories.PhotoRepository
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class GetPhotoUseCaseTest {
    @Mock lateinit var mockPhotoRepository: PhotoRepository
    @Mock lateinit var mockPhotoSingle: Single<Photo>
    lateinit var useCase: GetPhotoUseCase

    @Before
    fun setup() {
        useCase = GetPhotoUseCase(mockPhotoRepository)
    }

    @Test
    fun execute() {
        val photoId = UUID.randomUUID()
        whenever(mockPhotoRepository.get(photoId)).thenReturn(mockPhotoSingle)
        assertEquals(useCase.execute(photoId), mockPhotoSingle)
    }
}

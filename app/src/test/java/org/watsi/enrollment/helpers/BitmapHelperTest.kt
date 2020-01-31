package org.watsi.enrollment.helpers

import android.graphics.Bitmap
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.ByteArrayOutputStream

@RunWith(MockitoJUnitRunner::class)
class BitmapHelperTest {
    @Mock lateinit var mockThumbnailExtractor: BitmapHelper.ThumbnailExtractor
    @Mock lateinit var mockThumbnailBitmap: Bitmap

    @Test
    fun cropByteArray() {
        val byteArray = ByteArray(100000, {0x3})
        val stream = ByteArrayOutputStream()
        whenever(mockThumbnailExtractor.extractThumbnail(any(), any(), any())).thenReturn(mockThumbnailBitmap)
        val newArray = BitmapHelper.cropByteArray(byteArray, 240, 240, mockThumbnailExtractor, stream)
        assertNotNull(newArray)
        verify(mockThumbnailExtractor).extractThumbnail(byteArray, 240, 240)
        verify(mockThumbnailBitmap).compress(Bitmap.CompressFormat.JPEG, 70, stream)
    }
}

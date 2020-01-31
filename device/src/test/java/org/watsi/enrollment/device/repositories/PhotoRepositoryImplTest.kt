package org.watsi.enrollment.device.repositories

import android.database.sqlite.SQLiteException
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import okhttp3.RequestBody
import okio.Buffer
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.enrollment.device.api.CoverageApi
import org.watsi.enrollment.device.db.daos.PhotoDao
import org.watsi.enrollment.device.db.models.MemberWithRawPhotoModel
import org.watsi.enrollment.device.db.models.PhotoModel
import org.watsi.enrollment.device.factories.MemberModelFactory
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.entities.AuthenticationToken
import org.watsi.enrollment.domain.entities.Delta
import org.watsi.enrollment.domain.factories.PhotoFactory
import org.watsi.enrollment.domain.factories.UserFactory
import java.util.Arrays
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class PhotoRepositoryImplTest {
    @Mock lateinit var mockDao: PhotoDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    val user = UserFactory.build()
    val token = AuthenticationToken("token", clock.instant(), user)
    val photo = PhotoFactory.build()
    val photoModel = PhotoModel.fromPhoto(photo, clock)
    lateinit var repo: PhotoRepositoryImpl

    @Before
    fun setup() {
        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        repo = PhotoRepositoryImpl(mockDao, mockApi, mockSessionManager, clock)
    }

    @Test
    fun get_isPersisted_returnsPhoto() {
        whenever(mockDao.get(photo.id)).thenReturn(Single.just(photoModel))

        repo.get(photo.id).test().assertValue(photo)
    }

    @Test
    fun get_throwsException_callsOnError() {
        val photoId = UUID.randomUUID()
        val exception = SQLiteException()
        whenever(mockDao.get(photoId)).thenReturn(Single.error(exception))

        repo.get(photoId).test().assertError(exception)
    }

    @Test
    fun insert_completes() {
        repo.insert(photo).test().assertComplete()

        verify(mockDao).insert(photoModel)
    }

    @Test
    fun sync() {
        val memberModel = MemberModelFactory.build(
            householdId = UUID.randomUUID(),
            photoId = photo.id
        )
        val delta = Delta(action = Delta.Action.ADD,
                          modelName = Delta.ModelName.PHOTO,
                          modelId = memberModel.id,
                          synced = false)
        val memberWithRawPhotoModel = MemberWithRawPhotoModel(memberModel, listOf(photoModel))
        whenever(mockDao.getMemberWithRawPhoto(memberModel.id))
                .thenReturn(Single.just(memberWithRawPhotoModel))
        val captor = argumentCaptor<RequestBody>()
        whenever(mockApi.patchPhoto(eq(token.getHeaderString()),
                                    eq(memberModel.id),
                                    captor.capture())).thenReturn(Single.just(mock()))

        repo.sync(listOf(delta)).test().assertComplete()

        val requestBody = captor.firstValue
        val buffer = Buffer()
        requestBody.writeTo(buffer)
        assertTrue(Arrays.equals(photo.bytes, buffer.readByteArray()))
    }
}

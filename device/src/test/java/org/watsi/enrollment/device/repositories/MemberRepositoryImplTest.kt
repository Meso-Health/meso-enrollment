package org.watsi.enrollment.device.repositories

import android.database.sqlite.SQLiteException
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import edu.emory.mathcs.backport.java.util.Arrays
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.enrollment.device.api.CoverageApi
import org.watsi.enrollment.device.db.daos.MemberDao
import org.watsi.enrollment.device.db.daos.PhotoDao
import org.watsi.enrollment.device.db.models.DeltaModel
import org.watsi.enrollment.device.db.models.MemberModel
import org.watsi.enrollment.device.db.models.MemberWithThumbnailModel
import org.watsi.enrollment.device.db.models.PhotoModel
import org.watsi.enrollment.device.factories.MemberModelFactory
import org.watsi.enrollment.device.factories.PhotoModelFactory
import org.watsi.enrollment.device.managers.PreferencesManager
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.domain.factories.DeltaFactory
import org.watsi.enrollment.domain.factories.MemberFactory
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class MemberRepositoryImplTest {
    @Mock lateinit var mockMemberDao: MemberDao
    @Mock lateinit var mockPhotoDao: PhotoDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    @Mock lateinit var mockPreferencesManager: PreferencesManager
    @Mock lateinit var okHttpClient: OkHttpClient
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    val thumbnailPhotoId = UUID.randomUUID()
    val memberModel: MemberModel = MemberModelFactory.build(
        householdId = UUID.randomUUID(),
        thumbnailPhotoId = thumbnailPhotoId,
        clock = clock
    )
    val member = memberModel.toMember()
    val photoModel = PhotoModelFactory.build(id = thumbnailPhotoId)
    val memberWithThumbnailModel = MemberWithThumbnailModel(memberModel, listOf(photoModel))
    val memberWithThumbnail = memberWithThumbnailModel.toMemberWithThumbnail()
    lateinit var memberRepo: MemberRepositoryImpl

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        memberRepo = MemberRepositoryImpl(mockMemberDao, mockPhotoDao, mockApi, mockSessionManager, clock, okHttpClient, mockPreferencesManager)
    }

    @Test
    fun getFlowable_memberIsNotPersisted_returnsEmpty() {
        val memberId = UUID.randomUUID()

        whenever(mockMemberDao.getFlowable(memberId)).thenReturn(Flowable.empty())
        memberRepo.getFlowable(memberId).test().assertResult()
    }

    @Test
    fun getFlowable_memberIsPersisted_returnsMember() {
        val memberId = member.id

        whenever(mockMemberDao.getFlowable(memberId))
                .thenReturn(Flowable.just(memberWithThumbnailModel))
        memberRepo.getFlowable(memberId).test().assertResult(memberWithThumbnail)
    }

    @Test
    fun getFlowable_throwsException_callsOnError() {
        val memberId = UUID.randomUUID()
        whenever(mockMemberDao.getFlowable(memberId)).thenReturn(Flowable.error(SQLiteException()))

        memberRepo.getFlowable(memberId).test().assertFailure(SQLiteException::class.java)
    }

    @Test
    fun create_memberDaoThrowsException_callsOnError() {
        val delta = DeltaFactory.build()
        whenever(mockMemberDao.insertWithDeltas(any(), any())).thenThrow(SQLiteException())

        memberRepo.create(member, listOf(delta)).test().assertFailure(SQLiteException::class.java)
    }

    @Test
    fun create_validArgs_completes() {
        val delta = DeltaFactory.build()

        memberRepo.create(member, listOf(delta)).test().assertComplete()

        val deltaModel = DeltaModel.fromDelta(delta, clock)
        verify(mockMemberDao).insertWithDeltas(memberModel, listOf(deltaModel))
    }

    @Test
    fun update_isSuccessful_completes() {
        val delta = DeltaFactory.build()

        memberRepo.update(member, listOf(delta)).test().assertComplete()

        verify(mockMemberDao).updateWithDeltas(
                MemberModel.fromMember(member, clock), listOf(DeltaModel.fromDelta(delta, clock)))
    }

    @Test
    fun update_throwsException_callsOnError() {
        whenever(mockMemberDao.updateWithDeltas(any(), any())).thenThrow(SQLiteException())

        memberRepo.update(member, emptyList()).test().assertFailure(SQLiteException::class.java)
    }

    @Test
    fun withCardId_isSuccessful_returnsNoMembers() {
        whenever(mockMemberDao.withCardId(any())).thenReturn(Maybe.empty())
        memberRepo.withCardId("RWI123123").test().assertResult()
    }

    @Test
    fun withCardId_isSuccessful_returnsMember() {
        whenever(mockMemberDao.withCardId(any())).thenReturn(Maybe.just(memberModel))
        memberRepo.withCardId("RWI123123").test().assertResult(member)
    }


    @Test
    fun downloadPhotos() {
        val photoUrl = "/dragonfly/media/foo-9ce2ca927c19c2b0"
        val photoBytes = ByteArray(1, { 0xa })
        val member = MemberFactory.build(photoUrl = photoUrl)
        val responseBody = ResponseBody.create(MediaType.parse("image/jpeg"), photoBytes)
        whenever(mockMemberDao.needPhotoDownload()).thenReturn(
            Single.just(listOf(MemberModel.fromMember(member, clock))))
        whenever(mockApi.fetchPhoto(photoUrl)).thenReturn(Single.just(responseBody))

        memberRepo.downloadPhotos().test().assertComplete()

        val captor = argumentCaptor<PhotoModel>()
        verify(mockPhotoDao).insert(captor.capture())
        val photo = captor.firstValue
        assert(Arrays.equals(photoBytes, photo.bytes))
        verify(mockMemberDao).upsert(MemberModel.fromMember(member.copy(thumbnailPhotoId = photo.id), clock))
        verify(mockPreferencesManager).updateMemberPhotosLastFetched(clock.instant())
    }

//    @Test
//    fun sync_post() {
//        val delta = DeltaFactory.build(action = Delta.Action.ADD,
//                modelName = Delta.ModelName.MEMBER,
//                modelId = member.id,
//                synced = false)
//        whenever(mockMemberDao.getByHouseholdId(member.id)).thenReturn(Single.just(memberModel))
//        whenever(mockApi.postMember(token.getHeaderString(), MemberApi(member)))
//                .thenReturn(Single.just(mock()))
//
//        memberRepo.sync(listOf(delta)).test().assertComplete()
//    }
//
//    @Test
//    fun sync_patch() {
//        val deltas = listOf("name", "gender").map { field ->
//            Delta(action = Delta.Action.EDIT,
//                  modelName = Delta.ModelName.MEMBER,
//                  modelId = member.id,
//                  field = field,
//                  synced = false)
//        }
//        whenever(mockMemberDao.getByHouseholdId(member.id)).thenReturn(Single.just(memberModel))
//        whenever(mockApi.patchMember(
//                token.getHeaderString(), member.id, MemberApi.patch(member, deltas)))
//                .thenReturn(Single.just(mock()))
//
//        memberRepo.sync(deltas).test().assertComplete()
//    }
}

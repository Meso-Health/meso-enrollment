package org.watsi.enrollment.di.modules

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.threeten.bp.Clock
import org.watsi.enrollment.device.api.CoverageApi
import org.watsi.enrollment.device.db.daos.AdministrativeDivisionDao
import org.watsi.enrollment.device.db.daos.DeltaDao
import org.watsi.enrollment.device.db.daos.EnrollmentPeriodDao
import org.watsi.enrollment.device.db.daos.HouseholdDao
import org.watsi.enrollment.device.db.daos.HouseholdEnrollmentRecordDao
import org.watsi.enrollment.device.db.daos.MemberDao
import org.watsi.enrollment.device.db.daos.MemberEnrollmentRecordDao
import org.watsi.enrollment.device.db.daos.MembershipPaymentDao
import org.watsi.enrollment.device.db.daos.PhotoDao
import org.watsi.enrollment.device.db.daos.SummaryStatsDao
import org.watsi.enrollment.device.managers.PreferencesManager
import org.watsi.enrollment.device.managers.SessionManager
import org.watsi.enrollment.device.repositories.AdministrativeDivisionRepositoryImpl
import org.watsi.enrollment.device.repositories.DeltaRepositoryImpl
import org.watsi.enrollment.device.repositories.EnrollmentPeriodRepositoryImpl
import org.watsi.enrollment.device.repositories.HouseholdEnrollmentRecordRepositoryImpl
import org.watsi.enrollment.device.repositories.HouseholdRepositoryImpl
import org.watsi.enrollment.device.repositories.MemberEnrollmentRecordRepositoryImpl
import org.watsi.enrollment.device.repositories.MemberRepositoryImpl
import org.watsi.enrollment.device.repositories.MembershipPaymentRepositoryImpl
import org.watsi.enrollment.device.repositories.PhotoRepositoryImpl
import org.watsi.enrollment.device.repositories.SummaryStatsRepositoryImpl
import org.watsi.enrollment.domain.repositories.AdministrativeDivisionRepository
import org.watsi.enrollment.domain.repositories.DeltaRepository
import org.watsi.enrollment.domain.repositories.EnrollmentPeriodRepository
import org.watsi.enrollment.domain.repositories.HouseholdEnrollmentRecordRepository
import org.watsi.enrollment.domain.repositories.HouseholdRepository
import org.watsi.enrollment.domain.repositories.MemberEnrollmentRecordRepository
import org.watsi.enrollment.domain.repositories.MemberRepository
import org.watsi.enrollment.domain.repositories.MembershipPaymentRepository
import org.watsi.enrollment.domain.repositories.PhotoRepository
import org.watsi.enrollment.domain.repositories.SummaryStatsRepository

@Module
class RepositoryModule {
    @Provides
    fun provideMemberRepositoryImpl(memberDao: MemberDao,
                                    photoDao: PhotoDao,
                                    api: CoverageApi,
                                    sessionManager: SessionManager,
                                    clock: Clock,
                                    okHttpClient: OkHttpClient,
                                    preferencesManager: PreferencesManager): MemberRepositoryImpl {
        return MemberRepositoryImpl(memberDao, photoDao, api, sessionManager, clock, okHttpClient, preferencesManager)
    }

    @Provides
    fun provideMemberRepository(memberRepositoryImpl: MemberRepositoryImpl): MemberRepository {
        return memberRepositoryImpl
    }

    @Provides
    fun provideHouseholdRepositoryImpl(householdDao: HouseholdDao,
                                       deltaDao: DeltaDao,
                                       memberDao: MemberDao,
                                       api: CoverageApi,
                                       sessionManager: SessionManager,
                                       clock: Clock,
                                       okHttpClient: OkHttpClient,
                                       preferencesManager: PreferencesManager): HouseholdRepository {
        return HouseholdRepositoryImpl(
                householdDao, deltaDao, memberDao, api, sessionManager, clock, okHttpClient, preferencesManager)
    }

    @Provides
    fun providePhotoRepository(photoDao: PhotoDao,
                               api: CoverageApi,
                               sessionManager: SessionManager,
                               clock: Clock): PhotoRepository {
        return PhotoRepositoryImpl(photoDao, api, sessionManager, clock)
    }

    @Provides
    fun provideMembershipPaymentRepository(membershipPaymentDao: MembershipPaymentDao,
                                           api: CoverageApi,
                                           sessionManager: SessionManager,
                                           clock: Clock,
                                           okHttpClient: OkHttpClient): MembershipPaymentRepository {
        return MembershipPaymentRepositoryImpl(membershipPaymentDao, api, clock, sessionManager, okHttpClient)
    }

    @Provides
    fun provideDeltaRepository(deltaDao: DeltaDao, clock: Clock, okHttpClient: OkHttpClient): DeltaRepository {
        return DeltaRepositoryImpl(deltaDao, clock, okHttpClient)
    }

    @Provides
    fun provideHouseholdEnrollmentRecordRepository(
        householdEnrollmentRecordDao: HouseholdEnrollmentRecordDao,
        api: CoverageApi,
        clock: Clock,
        sessionManager: SessionManager,
        okHttpClient: OkHttpClient
    ): HouseholdEnrollmentRecordRepository {
        return HouseholdEnrollmentRecordRepositoryImpl(householdEnrollmentRecordDao, api, clock, sessionManager, okHttpClient)
    }

    @Provides
    fun provideMemberEnrollmentRecordRepository(
        memberEnrollmentRecordDao: MemberEnrollmentRecordDao,
        api: CoverageApi,
        clock: Clock,
        sessionManager: SessionManager,
        okHttpClient: OkHttpClient,
        memberRepository: MemberRepository
    ): MemberEnrollmentRecordRepository {
        return MemberEnrollmentRecordRepositoryImpl(memberEnrollmentRecordDao, api, clock, sessionManager, okHttpClient, memberRepository)
    }

    @Provides
    fun provideAdministrativeDivisionRepository(
        administrativeDivisionDao: AdministrativeDivisionDao,
        api: CoverageApi,
        clock: Clock,
        sessionManager: SessionManager,
        preferencesManager: PreferencesManager,
        okHttpClient: OkHttpClient
    ): AdministrativeDivisionRepository {
        return AdministrativeDivisionRepositoryImpl(administrativeDivisionDao, api, sessionManager, clock, preferencesManager, okHttpClient)
    }

    @Provides
    fun provideSummaryStatsRepository(
        summaryStatsDao: SummaryStatsDao
    ): SummaryStatsRepository {
        return SummaryStatsRepositoryImpl(summaryStatsDao)
    }

    @Provides
    fun provideEnrollmentPeriodRepository(
        enrollmentPeriodDao: EnrollmentPeriodDao,
        api: CoverageApi,
        clock: Clock,
        sessionManager: SessionManager,
        okHttpClient: OkHttpClient
    ): EnrollmentPeriodRepository {
        return EnrollmentPeriodRepositoryImpl(enrollmentPeriodDao, api, sessionManager, okHttpClient, clock)
    }
}

package org.watsi.enrollment.di.modules

import dagger.Module
import dagger.Provides
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
import org.watsi.enrollment.domain.usecases.ArchiveMemberUseCase
import org.watsi.enrollment.domain.usecases.CreateMemberUseCase
import org.watsi.enrollment.domain.usecases.DeleteUserDataUseCase
import org.watsi.enrollment.domain.usecases.EnrollHouseholdUseCase
import org.watsi.enrollment.domain.usecases.EnrollMemberUseCase
import org.watsi.enrollment.domain.usecases.FetchAdministrativeDivisionsUseCase
import org.watsi.enrollment.domain.usecases.FetchEnrollmentPeriodsUseCase
import org.watsi.enrollment.domain.usecases.FetchHouseholdsUseCase
import org.watsi.enrollment.domain.usecases.FetchMemberPhotosUseCase
import org.watsi.enrollment.domain.usecases.FetchStatusUseCase
import org.watsi.enrollment.domain.usecases.FindHouseholdIdByMembershipNumberUseCase
import org.watsi.enrollment.domain.usecases.LoadAdministrativeDivisionsUseCase
import org.watsi.enrollment.domain.usecases.LoadCurrentEnrollmentPeriodUseCase
import org.watsi.enrollment.domain.usecases.LoadHouseholdUseCase
import org.watsi.enrollment.domain.usecases.LoadHouseholdsUseCase
import org.watsi.enrollment.domain.usecases.LoadMemberUseCase
import org.watsi.enrollment.domain.usecases.LoadMembersWithCardUseCase
import org.watsi.enrollment.domain.usecases.LoadPhotoUseCase
import org.watsi.enrollment.domain.usecases.LoadSummaryStatisticsUseCase
import org.watsi.enrollment.domain.usecases.LoadUnpersistedMembersWithThumbnailsUseCase
import org.watsi.enrollment.domain.usecases.RenewHouseholdUseCase
import org.watsi.enrollment.domain.usecases.RestoreMemberUseCase
import org.watsi.enrollment.domain.usecases.SaveHouseholdUseCase
import org.watsi.enrollment.domain.usecases.SaveMembershipPaymentUseCase
import org.watsi.enrollment.domain.usecases.SyncHouseholdEnrollmentRecordUseCase
import org.watsi.enrollment.domain.usecases.SyncHouseholdUseCase
import org.watsi.enrollment.domain.usecases.SyncMemberEnrollmentRecordUseCase
import org.watsi.enrollment.domain.usecases.SyncMemberUseCase
import org.watsi.enrollment.domain.usecases.SyncMembershipPaymentUseCase
import org.watsi.enrollment.domain.usecases.SyncPhotoUseCase
import org.watsi.enrollment.domain.usecases.SyncStatusUseCase
import org.watsi.enrollment.domain.usecases.UpdateMemberUseCase
import org.watsi.enrollment.domain.usecases.VerifyCurrentEnrollmentPeriodExistsUseCase

@Module
class UseCaseModule {
    @Provides
    fun provideLoadMemberUseCase(memberRepository: MemberRepository): LoadMemberUseCase {
        return LoadMemberUseCase(memberRepository)
    }

    @Provides
    fun provideLoadHouseholdUseCase(householdRepository: HouseholdRepository): LoadHouseholdUseCase {
        return LoadHouseholdUseCase(householdRepository)
    }

    @Provides
    fun provideCreateMemberUseCase(memberRepository: MemberRepository): CreateMemberUseCase {
        return CreateMemberUseCase(memberRepository)
    }

    @Provides
    fun provideUpdateMemberUseCase(memberRepository: MemberRepository): UpdateMemberUseCase {
        return UpdateMemberUseCase(memberRepository)
    }

    @Provides
    fun provideSaveHouseholdUseCase(householdRepository: HouseholdRepository): SaveHouseholdUseCase {
        return SaveHouseholdUseCase(householdRepository)
    }

    @Provides
    fun provideLoadHouseholdsUseCase(householdRepository: HouseholdRepository):
            LoadHouseholdsUseCase {
        return LoadHouseholdsUseCase(householdRepository)
    }

    @Provides
    fun provideLoadPhotoUseCase(photoRepository: PhotoRepository): LoadPhotoUseCase {
        return LoadPhotoUseCase(photoRepository)
    }

    @Provides
    fun provideLoadMembersWithCardUseCase(memberRepository: MemberRepository):
            LoadMembersWithCardUseCase {
        return LoadMembersWithCardUseCase(memberRepository)
    }

    @Provides
    fun provideFetchHouseholdsUseCase(householdRepository: HouseholdRepository):
            FetchHouseholdsUseCase {
        return FetchHouseholdsUseCase(householdRepository)
    }

    @Provides
    fun provideFetchMemberPhotosUseCase(memberRepository: MemberRepository): FetchMemberPhotosUseCase {
        return FetchMemberPhotosUseCase(memberRepository)
    }

    @Provides
    fun provideSyncHouseholdEnrollmentRecordUseCase(
        householdEnrollmentRecordRepository: HouseholdEnrollmentRecordRepository,
        deltaRepository: DeltaRepository
    ): SyncHouseholdEnrollmentRecordUseCase {
        return SyncHouseholdEnrollmentRecordUseCase(householdEnrollmentRecordRepository, deltaRepository)
    }

    @Provides
    fun provideSyncMemberEnrollmentRecordUseCase(
        memberEnrollmentRecordRepository: MemberEnrollmentRecordRepository,
        deltaRepository: DeltaRepository
    ): SyncMemberEnrollmentRecordUseCase {
        return SyncMemberEnrollmentRecordUseCase(memberEnrollmentRecordRepository, deltaRepository)
    }

    @Provides
    fun provideSyncMembershipPaymentUseCase(
        membershipPaymentRepository: MembershipPaymentRepository,
        deltaRepository: DeltaRepository
    ): SyncMembershipPaymentUseCase {
        return SyncMembershipPaymentUseCase(membershipPaymentRepository, deltaRepository)
    }

    @Provides
    fun provideSyncHouseholdUseCase(householdRepository: HouseholdRepository,
                                    deltaRepository: DeltaRepository): SyncHouseholdUseCase {
        return SyncHouseholdUseCase(householdRepository, deltaRepository)
    }

    @Provides
    fun provideSyncMemberUseCase(memberRepository: MemberRepository,
                                 deltaRepository: DeltaRepository): SyncMemberUseCase {
        return SyncMemberUseCase(memberRepository, deltaRepository)
    }

    @Provides
    fun provideSyncPhotoUseCase(photoRepository: PhotoRepository,
                                deltaRepository: DeltaRepository): SyncPhotoUseCase {
        return SyncPhotoUseCase(photoRepository, deltaRepository)
    }

    @Provides
    fun provideSyncStatusUseCase(deltaRepository: DeltaRepository): SyncStatusUseCase {
        return SyncStatusUseCase(deltaRepository)
    }

    @Provides
    fun provideFetchStatusUseCase(memberRepository: MemberRepository): FetchStatusUseCase {
        return FetchStatusUseCase(memberRepository)
    }

    @Provides
    fun provideFindHouseholdIdByMembershipNumberUseCase(memberRepository: MemberRepository): FindHouseholdIdByMembershipNumberUseCase {
        return FindHouseholdIdByMembershipNumberUseCase(memberRepository)
    }
    

    @Provides
    fun provideSaveMembershipPaymentUseCase(
            membershipPaymentRepository: MembershipPaymentRepository,
            updateMemberUseCase: UpdateMemberUseCase
    ): SaveMembershipPaymentUseCase {
        return SaveMembershipPaymentUseCase(membershipPaymentRepository, updateMemberUseCase)
    }

    @Provides
    fun provideRenewHouseholdUseCase(
        householdEnrollmentRecordRepository: HouseholdEnrollmentRecordRepository,
        membershipPaymentRepository: MembershipPaymentRepository,
        membershipEnrollmentRecordRepository: MemberEnrollmentRecordRepository,
        updateMemberUseCase: UpdateMemberUseCase,
        loadCurrentEnrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase
    ): RenewHouseholdUseCase {
        return RenewHouseholdUseCase(
            householdEnrollmentRecordRepository,
            membershipPaymentRepository,
            membershipEnrollmentRecordRepository,
            updateMemberUseCase,
            loadCurrentEnrollmentPeriodUseCase
        )
    }

    @Provides
    fun provideLoadUnpersistedMembersWithThumbnailsUseCase(loadPhotoUseCase: LoadPhotoUseCase): LoadUnpersistedMembersWithThumbnailsUseCase {
        return LoadUnpersistedMembersWithThumbnailsUseCase(loadPhotoUseCase)
    }

    @Provides
    fun provideArchiveMemberUseCase(memberRepository: MemberRepository): ArchiveMemberUseCase {
        return ArchiveMemberUseCase(memberRepository)
    }

    @Provides
    fun provideRestoreMemberUseCase(memberRepository: MemberRepository): RestoreMemberUseCase {
        return RestoreMemberUseCase(memberRepository)
    }

    @Provides
    fun provideEnrollHouseholdUseCase(
        householdRepository: HouseholdRepository,
        householdEnrollmentRecordRepository: HouseholdEnrollmentRecordRepository,
        createMemberUseCase: CreateMemberUseCase,
        memberEnrollmentRecordRepository: MemberEnrollmentRecordRepository,
        membershipPaymentRepository: MembershipPaymentRepository,
        loadCurrentEnrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase
    ): EnrollHouseholdUseCase {
        return EnrollHouseholdUseCase(
            householdRepository,
            householdEnrollmentRecordRepository,
            createMemberUseCase,
            memberEnrollmentRecordRepository,
            membershipPaymentRepository,
            loadCurrentEnrollmentPeriodUseCase
        )
    }

    @Provides
    fun provideEnrollMemberUseCase(
        memberEnrollmentRecordRepository: MemberEnrollmentRecordRepository,
        createMemberUseCase: CreateMemberUseCase
    ): EnrollMemberUseCase {
        return EnrollMemberUseCase(
            memberEnrollmentRecordRepository,
            createMemberUseCase
        )
    }

    @Provides
    fun provideLoadSummaryStatisticsUseCase(
        summaryStatsRepository: SummaryStatsRepository,
        enrollmentPeriodRepository: EnrollmentPeriodRepository
    ): LoadSummaryStatisticsUseCase {
        return LoadSummaryStatisticsUseCase(summaryStatsRepository, enrollmentPeriodRepository)
    }

    @Provides
    fun provideFetchAdministrativeDivisionsUseCase(
        administrativeDivisionRepository: AdministrativeDivisionRepository
    ): FetchAdministrativeDivisionsUseCase {
        return FetchAdministrativeDivisionsUseCase(administrativeDivisionRepository)
    }

    @Provides
    fun provideFetchEnrollmentPeriodsUseCase(
        enrollmentPeriodRepository: EnrollmentPeriodRepository
    ): FetchEnrollmentPeriodsUseCase {
        return FetchEnrollmentPeriodsUseCase(enrollmentPeriodRepository)
    }

    @Provides
    fun provideVerifyCurrentEnrollmentPeriodExistsUseCase(
        enrollmentPeriodRepository: EnrollmentPeriodRepository
    ): VerifyCurrentEnrollmentPeriodExistsUseCase {
        return VerifyCurrentEnrollmentPeriodExistsUseCase(enrollmentPeriodRepository)
    }

    @Provides
    fun provideLoadAdministrativeDivisionsUseCase(
        administrativeDivisionRepository: AdministrativeDivisionRepository
    ): LoadAdministrativeDivisionsUseCase {
        return LoadAdministrativeDivisionsUseCase(administrativeDivisionRepository)
    }

    @Provides
    fun provideLoadCurrentEnrollmentPeriodUseCase(
        enrollmentPeriodRepository: EnrollmentPeriodRepository
    ): LoadCurrentEnrollmentPeriodUseCase {
        return LoadCurrentEnrollmentPeriodUseCase(enrollmentPeriodRepository)
    }

    @Provides
    fun provideDeleteUserDataUseCase(
        deltaRepository: DeltaRepository,
        administrativeDivisionRepository: AdministrativeDivisionRepository,
        householdEnrollmentRecordRepository: HouseholdEnrollmentRecordRepository,
        householdRepository: HouseholdRepository,
        memberEnrollmentRecordRepository: MemberEnrollmentRecordRepository,
        memberRepository: MemberRepository,
        membershipPaymentRepository: MembershipPaymentRepository,
        enrollmentPeriodRepository: EnrollmentPeriodRepository
    ): DeleteUserDataUseCase {
        return DeleteUserDataUseCase(
            deltaRepository,
            administrativeDivisionRepository,
            householdEnrollmentRecordRepository,
            householdRepository,
            memberEnrollmentRecordRepository,
            memberRepository,
            membershipPaymentRepository,
            enrollmentPeriodRepository
        )
    }
}

package org.watsi.enrollment.di.modules

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import org.watsi.enrollment.device.db.AppDatabase
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
import javax.inject.Singleton

@Module
class DbModule {

    @Singleton
    @Provides
    fun provideDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "enrollment")
                .fallbackToDestructiveMigration()
                .build()
    }

    @Singleton
    @Provides
    fun provideMemberDao(db: AppDatabase): MemberDao = db.memberDao()

    @Singleton
    @Provides
    fun provideHouseholdDao(db: AppDatabase): HouseholdDao = db.householdDao()

    @Singleton
    @Provides
    fun provideMembershipPaymentDao(db: AppDatabase): MembershipPaymentDao = db.membershipPaymentDao()

    @Singleton
    @Provides
    fun providePhotoDao(db: AppDatabase): PhotoDao = db.photoDao()

    @Singleton
    @Provides
    fun provideDeltaDao(db: AppDatabase): DeltaDao = db.deltaDao()

    @Singleton
    @Provides
    fun provideMemberEnrollmentRecordDao(db: AppDatabase): MemberEnrollmentRecordDao = db.memberEnrollmentRecordDao()

    @Singleton
    @Provides
    fun provideHouseholdEnrollmentRecordDao(db: AppDatabase): HouseholdEnrollmentRecordDao = db.householdEnrollmentRecordDao()

    @Singleton
    @Provides
    fun provideAdministrativeDivisionDao(db: AppDatabase): AdministrativeDivisionDao = db.administrativeDivisionDao()

    @Singleton
    @Provides
    fun provideSummaryStatsDao(db: AppDatabase): SummaryStatsDao = db.summaryStatsDao()

    @Singleton
    @Provides
    fun provideEnrollmentPeriodDao(db: AppDatabase): EnrollmentPeriodDao = db.enrollmentPeriodDao()
}

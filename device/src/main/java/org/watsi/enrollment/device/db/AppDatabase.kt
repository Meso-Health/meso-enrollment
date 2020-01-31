package org.watsi.enrollment.device.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
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
import org.watsi.enrollment.device.db.models.AdministrativeDivisionModel
import org.watsi.enrollment.device.db.models.DeltaModel
import org.watsi.enrollment.device.db.models.EnrollmentPeriodModel
import org.watsi.enrollment.device.db.models.HouseholdEnrollmentRecordModel
import org.watsi.enrollment.device.db.models.HouseholdModel
import org.watsi.enrollment.device.db.models.MemberEnrollmentRecordModel
import org.watsi.enrollment.device.db.models.MemberModel
import org.watsi.enrollment.device.db.models.MembershipPaymentModel
import org.watsi.enrollment.device.db.models.PhotoModel

@Database(exportSchema = true, version = 6, entities = [
    MemberModel::class,
    HouseholdModel::class,
    PhotoModel::class,
    DeltaModel::class,
    MembershipPaymentModel::class,
    MemberEnrollmentRecordModel::class,
    HouseholdEnrollmentRecordModel::class,
    AdministrativeDivisionModel::class,
    EnrollmentPeriodModel::class
])
@TypeConverters(TypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun householdDao(): HouseholdDao
    abstract fun photoDao(): PhotoDao
    abstract fun membershipPaymentDao(): MembershipPaymentDao
    abstract fun deltaDao(): DeltaDao
    abstract fun memberEnrollmentRecordDao(): MemberEnrollmentRecordDao
    abstract fun householdEnrollmentRecordDao(): HouseholdEnrollmentRecordDao
    abstract fun administrativeDivisionDao(): AdministrativeDivisionDao
    abstract fun summaryStatsDao(): SummaryStatsDao
    abstract fun enrollmentPeriodDao(): EnrollmentPeriodDao
}

package org.watsi.enrollment.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import org.threeten.bp.LocalDate
import org.watsi.enrollment.device.db.models.EnrollmentPeriodModel

@Dao
interface EnrollmentPeriodDao {
    // If there is a gap between enrollment records, which should only happen if the enrollment periods
    // are mis-configured on the backend, this method will return the previous enrollment record.
    // If there are no enrollment periods to return, this will throw an exception. If this happens,
    // this enrollment app does not support any actions, other than ability to upload unsynced records.

    // Here are the specific scenarios:
    // months
    // M  1   2   3   4   5   6   7   8   9   10   11
    //        <--EP1--><--EP2->       <--EP3--->
    //
    // If time is:
    // - M1: throw exception
    // - M2: return enrollment period EP1
    // - M3: return enrollment period EP1
    // - M4: return enrollment period EP2
    // - M5: return enrollment period EP2
    // - M6: return enrollment period EP2
    // - M7: return enrollment period EP2
    // - M8: return enrollment period EP3
    // - M9: return enrollment period EP3
    // - M10: return enrollment period EP3
    // - M11: return enrollment period EP3
    @Query(
        "SELECT * FROM enrollmentPeriods WHERE :currentDate >= startDate ORDER BY endDate DESC LIMIT 1")
    // The return type of this is `Flowable<List<EnrollmentPeriodModel>>` instead of `Flowable<EnrollmentPeriodModel>`
    // because the latter will not emit anything in the case where there are no current enrollment periods.
    // That is the equivalent of Flowable.never(), which will cause the UI to freeze if a user logs in
    // while enrollment periods are misconfigured.
    // Instead, with the Flowable<List<>> return value, we will emit an empty row, and throw an error
    // in EnrollmentPeriodRepositoryImpl.current if there is an empty row.
    fun current(currentDate: LocalDate): Flowable<List<EnrollmentPeriodModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(models: List<EnrollmentPeriodModel>)

    @Query("DELETE from enrollmentPeriods")
    fun deleteAll()
}

package org.watsi.enrollment.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation

data class HouseholdEnrollmentRecordWithPaymentsModel(
    @Embedded var householdEnrollmentRecordModel: HouseholdEnrollmentRecordModel? = null,
    @Relation(parentColumn = "id", entityColumn = "householdEnrollmentRecordId", entity = MembershipPaymentModel::class)
    var payments: List<MembershipPaymentModel>? = null)

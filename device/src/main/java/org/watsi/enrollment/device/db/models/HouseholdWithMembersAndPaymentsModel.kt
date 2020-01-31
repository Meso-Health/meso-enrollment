package org.watsi.enrollment.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.enrollment.domain.relations.HouseholdWithMembersAndPayments

data class HouseholdWithMembersAndPaymentsModel(
    @Embedded var householdModel: HouseholdModel? = null,
    @Relation(parentColumn = "id", entityColumn = "householdId", entity = HouseholdEnrollmentRecordModel::class)
    var householdEnrollmentRecordsWithPayments: List<HouseholdEnrollmentRecordWithPaymentsModel>? = null,
    @Relation(parentColumn = "id", entityColumn = "householdId", entity = MemberModel::class)
    var membersWithThumbnails: List<MemberWithThumbnailModel>? = null,
    @Relation(parentColumn = "administrativeDivisionId", entityColumn = "id", entity = AdministrativeDivisionModel::class)
    var administrativeDivisions: List<AdministrativeDivisionModel>? = null) {

    fun toHouseholdWithMembersAndPayments(): HouseholdWithMembersAndPayments {
        householdModel?.let {
            val members = membersWithThumbnails?.map { it.toMemberWithThumbnail() } ?: emptyList()
            val householdEnrollmentRecords = householdEnrollmentRecordsWithPayments?.mapNotNull {
                it.householdEnrollmentRecordModel?.toHouseholdEnrollmentRecord()
            } ?: emptyList()
            val payments = householdEnrollmentRecordsWithPayments?.mapNotNull {
                it.payments?.map { it.toMembershipPayment() }
            }?.flatten() ?: emptyList()
            val administrativeDivision = administrativeDivisions?.
                    firstOrNull()?.
                    toAdministrativeDivision()

            return HouseholdWithMembersAndPayments(
                household = it.toHousehold(),
                householdEnrollmentRecords = householdEnrollmentRecords,
                members = members,
                payments = payments,
                administrativeDivision = administrativeDivision
            )
        }

        throw IllegalStateException("HouseholdModel cannot be null")
    }
}

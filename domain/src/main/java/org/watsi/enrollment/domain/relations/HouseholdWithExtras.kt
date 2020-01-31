package org.watsi.enrollment.domain.relations

import org.watsi.enrollment.domain.entities.Household
import org.watsi.enrollment.domain.entities.HouseholdEnrollmentRecord
import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.MemberEnrollmentRecord
import org.watsi.enrollment.domain.entities.MembershipPayment
import java.io.Serializable

data class HouseholdWithExtras(
    val household: Household,
    val activeHouseholdEnrollmentRecord: HouseholdEnrollmentRecord?,
    val members: List<Member>,
    val memberEnrollmentRecords: List<MemberEnrollmentRecord>,
    val membershipPayments: List<MembershipPayment>
): Serializable

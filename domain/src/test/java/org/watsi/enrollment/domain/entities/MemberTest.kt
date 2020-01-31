package org.watsi.enrollment.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.watsi.enrollment.domain.entities.Member.Companion.ARCHIVED_REASON_UNPAID
import org.watsi.enrollment.domain.factories.EnrollmentPeriodFactory
import org.watsi.enrollment.domain.factories.HouseholdEnrollmentRecordFactory
import org.watsi.enrollment.domain.factories.HouseholdWithMembersAndPaymentsFactory
import org.watsi.enrollment.domain.factories.MemberFactory
import org.watsi.enrollment.domain.factories.MembershipPaymentFactory
import org.watsi.enrollment.domain.relations.MemberWithThumbnail
import java.util.UUID

@RunWith(JUnit4::class)
class MemberTest {
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

    @Test
    fun copyAndUpdatePaymentStatus_paidAndNotArchived_expectNoChange() {
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(paying = true)
        val payment = MembershipPaymentFactory.build(
            annualContributionFee = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE,
            qualifyingBeneficiariesFee = PaymentFees.ANNUAL_BENEFICIARY_FEE * 2,
            householdEnrollmentRecordId = householdEnrollmentRecord.id
        )
        val member = MemberFactory.build(
            archivedReason = null,
            archivedAt = null
        )
        val memberList = listOf(
            MemberFactory.headOfHousehold(),
            MemberFactory.qualifyingBeneficiary(),
            member
        )
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            members = memberList.map { MemberWithThumbnail(it, null) },
            payments = listOf(payment),
            householdEnrollmentRecords = listOf(householdEnrollmentRecord)
        )
        val enrollmentPeriod = EnrollmentPeriodFactory.build()
        val updatedMember = member.copyAndUpdatePaymentStatus(
            householdWithMembersAndPayments, enrollmentPeriod, clock
        )
        assertNull(updatedMember.archivedAt)
        assertNull(updatedMember.archivedReason)
    }

    @Test
    fun copyAndUpdatePaymentStatus_unPaidAndNotArchived_expectArchive() {
        val member = MemberFactory.build(
            birthdate = LocalDate.of(1975, 5, 5),
            profession = "farmer",
            archivedReason = null,
            archivedAt = null
        )
        val memberList = listOf(
            MemberFactory.headOfHousehold(),
            MemberFactory.qualifyingBeneficiary(),
            MemberFactory.qualifyingBeneficiary(),
            member
        )
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(paying = true)
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            members = memberList.map { MemberWithThumbnail(it, null) },
            payments = emptyList(),
            householdEnrollmentRecords = listOf(householdEnrollmentRecord)
        )
        val enrollmentPeriod = EnrollmentPeriodFactory.build()

        val updatedMember = member.copyAndUpdatePaymentStatus(
            householdWithMembersAndPayments, enrollmentPeriod, clock)

        assertNotNull(updatedMember.archivedAt)
        assertEquals(ARCHIVED_REASON_UNPAID, updatedMember.archivedReason)
    }

    @Test
    fun copyAndUpdatePaymentStatus_paidAndArchived_expectRestore() {
        val enrollmentPeriod = EnrollmentPeriodFactory.build()
        val householdEnrollmentRecord = HouseholdEnrollmentRecordFactory.build(
            paying = true,
            enrollmentPeriodId = enrollmentPeriod.id
        )
        val previousPayment = MembershipPaymentFactory.build(
            annualContributionFee = PaymentFees.ANNUAL_HEAD_OF_HOUSEHOLD_FEE,
            qualifyingBeneficiariesFee = PaymentFees.ANNUAL_BENEFICIARY_FEE * 2,
            householdEnrollmentRecordId = householdEnrollmentRecord.id
        )
        val member = MemberFactory.build(
            birthdate = LocalDate.of(1975, 5, 5),
            profession = "farmer",
            archivedReason = ARCHIVED_REASON_UNPAID,
            archivedAt = Instant.now()
        )
        val memberList = listOf(
            MemberFactory.headOfHousehold(),
            MemberFactory.qualifyingBeneficiary(),
            member
        )
        val householdWithMembersAndPayments = HouseholdWithMembersAndPaymentsFactory.build(
            members = memberList.map { MemberWithThumbnail(it, null) },
            payments = listOf(previousPayment),
            householdEnrollmentRecords = listOf(householdEnrollmentRecord)
        )

        val updatedMember = member.copyAndUpdatePaymentStatus(
            householdWithMembersAndPayments, enrollmentPeriod, clock)

        assertNull(updatedMember.archivedAt)
        assertNull(updatedMember.archivedReason)
    }

    @Test
    fun copyAndRestore() {
        val member = MemberFactory.build(archivedAt = Instant.now(), archivedReason = ARCHIVED_REASON_UNPAID)
        val restoredMember = member.copyAndRestore()

        assertNull(restoredMember.archivedAt)
        assertNull(restoredMember.archivedReason)
    }

    @Test
    fun isValidFullName() {
        assertFalse(Member.isValidFullName("", 1))
        assertFalse(Member.isValidFullName("", 2))
        assertFalse(Member.isValidFullName("", 3))

        assertFalse(Member.isValidFullName("   ", 1))
        assertFalse(Member.isValidFullName("   ", 2))
        assertFalse(Member.isValidFullName("   ", 3))

        assertTrue(Member.isValidFullName("Michael", 1))
        assertFalse(Member.isValidFullName("Michael", 2))
        assertFalse(Member.isValidFullName("Michael", 3))

        assertTrue(Member.isValidFullName(" Michael ", 1))
        assertFalse(Member.isValidFullName(" Michael ", 2))
        assertFalse(Member.isValidFullName(" Michael ", 3))

        assertTrue(Member.isValidFullName("   Michael", 1))
        assertFalse(Member.isValidFullName("   Michael", 2))
        assertFalse(Member.isValidFullName("   Michael", 3))

        assertTrue(Member.isValidFullName("Michael   ", 1))
        assertFalse(Member.isValidFullName("Michael   ", 2))
        assertFalse(Member.isValidFullName("Michael   ", 3))

        assertTrue(Member.isValidFullName("Michael Jordan", 1))
        assertTrue(Member.isValidFullName("Michael Jordan", 2))
        assertFalse(Member.isValidFullName("Michael Jordan", 3))

        assertTrue(Member.isValidFullName("  Michael B. ", 1))
        assertTrue(Member.isValidFullName("  Michael B. ", 2))
        assertFalse(Member.isValidFullName("  Michael B. ", 3))

        assertTrue(Member.isValidFullName("Michael Bakari Jordan", 1))
        assertTrue(Member.isValidFullName("Michael Bakari Jordan", 2))
        assertTrue(Member.isValidFullName("Michael Bakari Jordan", 3))

        assertTrue(Member.isValidFullName(" Michael B. J ", 1))
        assertTrue(Member.isValidFullName(" Michael B. J ", 2))
        assertTrue(Member.isValidFullName(" Michael B. J ", 3))

        assertTrue(Member.isValidFullName("Michael B. Jordan Jr", 1))
        assertTrue(Member.isValidFullName("Michael B. Jordan Jr", 2))
        assertTrue(Member.isValidFullName("Michael B. Jordan Jr", 3))
    }

    @Test
    fun isValidMedicalRecordNumber() {
        assertFalse(Member.isValidMedicalRecordNumber("123", 4, 7))
        assertTrue(Member.isValidMedicalRecordNumber("1234", 4, 7))
        assertTrue(Member.isValidMedicalRecordNumber("12345", 4, 7))
        assertTrue(Member.isValidMedicalRecordNumber("123456", 4, 7))
        assertTrue(Member.isValidMedicalRecordNumber("1234567", 4, 7))
        assertFalse(Member.isValidMedicalRecordNumber("12345678", 4, 7))
    }

    @Test
    fun isValidCardId() {
        assertTrue(Member.isValidCardId("HEY123123"))
        assertTrue(Member.isValidCardId("RWI123123"))
        assertFalse(Member.isValidCardId("RWI 123 123"))
        assertFalse(Member.isValidCardId("RWI123   123"))
        assertFalse(Member.isValidCardId("LALALA BANANAPHONE 123"))
        assertFalse(Member.isValidCardId(""))
        assertFalse(Member.isValidCardId("   "))
    }

    @Test
    fun diff_returnsDeltaForEachChangedField() {
        val member = MemberFactory.build()

        val deltas = member.diff(member.copy(name = "Diff Name",
                                             phoneNumber = null,
                                             photoId = UUID.randomUUID()))

        assertEquals(3, deltas.size)
        assertEquals(listOf(member.id), deltas.map { it.modelId }.distinct())
        assertEquals(listOf(Delta.Action.EDIT), deltas.map { it.action }.distinct())
        assertEquals(listOf(Delta.ModelName.MEMBER), deltas.map { it.modelName }.distinct())
        assertEquals(listOf(false), deltas.map { it.synced }.distinct())
        assertEquals(listOf("name", "phoneNumber", "photoId"), deltas.map { it.field }.distinct())
    }

    @Test
    fun diff_returnsDeltaForNulledFields() {
        val member = MemberFactory.build(archivedReason = ARCHIVED_REASON_UNPAID, archivedAt = Instant.now())

        val deltas = member.diff(member.copy(archivedReason = null, archivedAt = null))
        assertEquals(2, deltas.size)
        assertEquals(listOf(member.id), deltas.map { it.modelId }.distinct())
        assertEquals(listOf(Delta.Action.EDIT), deltas.map { it.action }.distinct())
        assertEquals(listOf(Delta.ModelName.MEMBER), deltas.map { it.modelName }.distinct())
        assertEquals(listOf(false), deltas.map { it.synced }.distinct())
        assertEquals(listOf("archivedAt", "archivedReason"), deltas.map { it.field }.distinct())

        // Check both directions
        val member2 = MemberFactory.build(archivedReason = null, archivedAt = null)

        val deltas2 = member2.diff(member2.copy(archivedReason = ARCHIVED_REASON_UNPAID, archivedAt = Instant.now()))
        assertEquals(2, deltas2.size)
        assertEquals(listOf(member2.id), deltas2.map { it.modelId }.distinct())
        assertEquals(listOf(Delta.Action.EDIT), deltas2.map { it.action }.distinct())
        assertEquals(listOf(Delta.ModelName.MEMBER), deltas2.map { it.modelName }.distinct())
        assertEquals(listOf(false), deltas2.map { it.synced }.distinct())
        assertEquals(listOf("archivedAt", "archivedReason"), deltas2.map { it.field }.distinct())
    }


    @Test
    fun deleted() {
        assertFalse(MemberFactory.build(archivedReason = null).archivedPermanently())
        assertFalse(MemberFactory.build(archivedReason = ARCHIVED_REASON_UNPAID).archivedPermanently())
        assertTrue(MemberFactory.build(archivedReason = "death").archivedPermanently())
    }
}

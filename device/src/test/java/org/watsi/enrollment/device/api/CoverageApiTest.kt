package org.watsi.enrollment.device.api

import okhttp3.Credentials
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.enrollment.device.testutils.OkReplayTest
import org.watsi.enrollment.domain.entities.DateAccuracy
import org.watsi.enrollment.domain.entities.Gender
import org.watsi.enrollment.domain.factories.AuthenticationTokenFactory
import java.util.UUID

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CoverageApiTest : OkReplayTest() {
    // Make sure this userId corresponds to a valid user in the backend.
    val userId = 93
    val enrollerUser = "enroller"
    val enrollerPassword = "password"
    // Make sure this token is valid when recording new tapes.
    val token = AuthenticationTokenFactory.build(token = "oZiQq34B.tVRPstiFudy2bMwC2zM1RKTH13pbDD11")
    // Make sure this is a valid enrollment period id in the backend.
    val enrollmentPeriodId = 2
    // Make sure this is a valid card in the backend.
    val cardId = "TST000000"

    // Make sure the following IDs don't exist in the local DB when recording new tapes.
    // Usually I just change a letter or number in each of the strings below (instead of deleting records from backend)
    val householdId = UUID.fromString("073e83f2-6e56-4bd8-a9df-ddf11802c9c3")
    val randomId = UUID.fromString("21fba022-22cd-4026-b673-7c8222f22b23")
    val householdEnrollmentRecordId = UUID.fromString("173e03f2-6e56-4bd3-a9df-ddf11802c9c6")
    val householdEnrollmentRecordId2 = UUID.fromString("10fba909-22cd-4036-b673-7c8200f21b11")
    val membershipPaymentId = UUID.fromString("74fba909-10cd-4026-b673-3c3200f21b15")
    val membershipPaymentId2 = UUID.fromString("e13bebff-0188-4dff-8d28-3ee3a3bcdc9e")
    val memberId = UUID.fromString("738a6308-08c1-4a4a-b103-7c8200f21b33")
    val memberEnrollmentRecordId = UUID.fromString("74fba009-22cd-4026-b333-a0ea46080d3d")
    val memberEnrollmentRecordId2 = UUID.fromString("25fba009-22cd-4026-b333-a0ea46080d9a")

    override fun afterSetup() {
        // no-op
    }

    @Test
    fun test000_login() {
        val result = api.login(
            authorization = Credentials.basic(enrollerUser, enrollerPassword)
        ).test()
        result.assertComplete()
        result.values().first().toAuthenticationToken()
    }

    @Test
    fun test001_syncHousehold_validRequest_completes() {
        val householdApi = SyncHouseholdApi(
            id = householdId,
            enrolledAt = Instant.parse("2018-03-23T08:10:36.306Z"),
            administrativeDivisionId = 1,
            address = "21b Baker St"
        )

        api.postHousehold(token.getHeaderString(), householdApi).test().assertComplete()
    }

    @Test
    fun test002_syncMember_validRequest_completes() {
        val memberApi = MemberApi(
            id = memberId,
            enrolledAt = Instant.parse("2018-03-23T08:10:36.307Z"),
            fullName = "Full Name",
            birthdate = LocalDate.of(1993, 5, 11),
            birthdateAccuracy = DateAccuracy.D,
            gender = Gender.F,
            phoneNumber = "123456",
            photoUrl = "localhost:5000/dragonfly/media/2oi3rj2iorj23oirj23orjo23irj23orj23or",
            cardId = cardId,
            membershipNumber = "328-483-948",
            medicalRecordNumber = null,
            householdId = householdId,
            profession = "farmer",
            relationshipToHead = "spouse",
            archivedAt = null,
            archivedReason = null
        )

        api.postMember(
            authorization = token.getHeaderString(),
            member = memberApi
        ).test().assertComplete()
    }

    @Test
    fun test003_syncHouseholdEnrollmentRecord_validRequest_completes() {
        val householdEnrollmentRecordApi = HouseholdEnrollmentRecordApi(
            id = householdEnrollmentRecordId,
            householdId = householdId,
            userId = userId,
            enrolledAt = Instant.parse("2018-03-23T08:10:36.307Z"),
            paying = true,
            renewal = false,
            administrativeDivisionId = 1,
            enrollmentPeriodId = enrollmentPeriodId
        )

        api.postHouseholdEnrollmentRecord(
            authorization = token.getHeaderString(),
            householdEnrollmentRecord = householdEnrollmentRecordApi
        ).test().assertComplete()
    }

    @Test
    fun test004_syncHouseholdEnrollmentRecord_invalidRequest_completes() {
        val householdIdThatDoesNotExist = randomId
        val householdEnrollmentRecordApi = HouseholdEnrollmentRecordApi(
            id = householdEnrollmentRecordId2,
            householdId = householdIdThatDoesNotExist,
            userId = userId,
            enrolledAt = Instant.parse("2018-03-23T08:10:36.307Z"),
            paying = true,
            renewal = false,
            administrativeDivisionId = 1,
            enrollmentPeriodId = enrollmentPeriodId
        )

        api.postHouseholdEnrollmentRecord(
            authorization = token.getHeaderString(),
            householdEnrollmentRecord = householdEnrollmentRecordApi
        ).test().assertError(Exception::class.java)
    }

    @Test
    fun test005_syncMembershipPayment_validRequest_completes() {
        val householdEnrollmentRecordApi = MembershipPaymentApi(
            id = membershipPaymentId,
            receiptNumber = "12345",
            paymentDate = LocalDate.of(2012, 1, 4),
            annualContributionFee = 2300,
            qualifyingBeneficiariesFee = 2000,
            registrationFee = 0,
            penaltyFee = 2000,
            cardReplacementFee = 2000,
            otherFee = 1234,
            householdEnrollmentRecordId = householdEnrollmentRecordId
        )

        api.postMembershipPayment(
            authorization = token.getHeaderString(),
            membershipPayment = householdEnrollmentRecordApi
        ).test().assertComplete()
    }

    @Test
    fun test006_syncMembershipPayment_invalidRequest_completes() {
        val householdEnrollmentRecordIdThatDoesNotExist = randomId
        val householdEnrollmentRecordApi = MembershipPaymentApi(
            id = membershipPaymentId2,
            receiptNumber = "12345",
            paymentDate = LocalDate.of(2012, 1, 4),
            annualContributionFee = 2300,
            qualifyingBeneficiariesFee = 2000,
            registrationFee = 0,
            penaltyFee = 2000,
            cardReplacementFee = 2000,
            otherFee = 1234,
            householdEnrollmentRecordId = householdEnrollmentRecordIdThatDoesNotExist
        )

        api.postMembershipPayment(
            authorization = token.getHeaderString(),
            membershipPayment = householdEnrollmentRecordApi
        ).test().assertError(Exception::class.java)
    }

    @Test
    fun test007_syncMemberEnrollmentRecord_validRequest_completes() {
        val memberEnrollmentRecordApi = MemberEnrollmentRecordApi(
            id = memberEnrollmentRecordId2,
            memberId = memberId,
            userId = userId,
            enrolledAt = Instant.parse("2018-03-23T08:10:36.307Z"),
            note = "member was not present during enrollment",
            enrollmentPeriodId = enrollmentPeriodId
        )

        api.postMemberEnrollmentRecord(
            authorization = token.getHeaderString(),
            memberEnrollmentRecord = memberEnrollmentRecordApi
        ).test().assertComplete()
    }

    @Test
    fun test008_syncMemberEnrollmentRecord_invalidRequest_completes() {
        val memberIdThatDoesNotExist = randomId
        val memberEnrollmentRecordApi = MemberEnrollmentRecordApi(
            id = memberEnrollmentRecordId,
            memberId = memberIdThatDoesNotExist,
            userId = userId,
            enrolledAt = Instant.parse("2018-03-23T08:10:36.307Z"),
            enrollmentPeriodId = enrollmentPeriodId,
            note = null
        )

        api.postMemberEnrollmentRecord(
            authorization = token.getHeaderString(),
            memberEnrollmentRecord = memberEnrollmentRecordApi
        ).test().assertError(Exception::class.java)
    }

    // This test might time out if there are too many households. Usually, I put a .limit(10) somewhere
    // in the backend controller so this assertComplete() actually passes instead of timing out.
    @Test
    fun test009_fetchHouseholds_validRequest_completes() {
        api.fetchHouseholds(
            tokenAuthorization = token.getHeaderString()
        ).test().assertComplete()
    }

    // commenting out for now - the requests work, but a custom boundary string is
    // created for each Multi-part string which I have not figured out a way to set,
    // which is causing OkReplay to not be able to match the body and so the tests fail
    // I'm still looking into being able to set the boundary...
//    @Test
//    fun patchPhoto_validRequest_completes() {
//        val requestBody = RequestBody.create(
//                MediaType.parse("image/jpg"), ByteArray(1, { 0xa }))
//
//        api.patchPhoto(token.getHeaderString(), memberId, requestBody).test().assertComplete()
//    }
//
//    @Test
//    fun patchPhoto_invalidRequest_errors() {
//        val bytes = ByteArray(1, { 0xa })
//        val requestBody = RequestBody.create(
//            MediaType.parse("image/jpg"),
//            bytes
//        )
//
//        api.patchPhoto(token.getHeaderString(), memberId, requestBody)
//                .test().assertError(Exception::class.java)
//    }
}

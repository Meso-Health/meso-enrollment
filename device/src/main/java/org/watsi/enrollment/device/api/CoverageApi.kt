package org.watsi.enrollment.device.api

import com.google.gson.JsonObject
import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.UUID

private const val AUTHORIZATION_HEADER = "Authorization"

interface CoverageApi {
    @POST("authentication_token")
    fun login(@Header(AUTHORIZATION_HEADER) authorization: String): Single<AuthenticationTokenApi>

    @GET("household_enrollment_records")
    fun fetchHouseholds(
        @Header(AUTHORIZATION_HEADER) tokenAuthorization: String
    ): Single<List<FetchHouseholdApi>>

    @GET
    fun fetchPhoto(@Url photoUrl: String): Single<ResponseBody>

    @POST("households")
    fun postHousehold(@Header(AUTHORIZATION_HEADER) authorization: String,
                      @Body household: SyncHouseholdApi): Single<SyncHouseholdApi>

    @POST("household_enrollment_records")
    fun postHouseholdEnrollmentRecord(
        @Header(AUTHORIZATION_HEADER) authorization: String,
        @Body householdEnrollmentRecord: HouseholdEnrollmentRecordApi
    ): Single<HouseholdEnrollmentRecordApi>

    @POST("members")
    fun postMember(@Header(AUTHORIZATION_HEADER) authorization: String,
                   @Body member: MemberApi): Single<MemberApi>

    @POST("member_enrollment_records")
    fun postMemberEnrollmentRecord(
        @Header(AUTHORIZATION_HEADER) authorization: String,
        @Body memberEnrollmentRecord: MemberEnrollmentRecordApi
    ): Single<MemberEnrollmentRecordApi>


    @POST("membership_payments")
    fun postMembershipPayment(
        @Header(AUTHORIZATION_HEADER) authorization: String,
        @Body membershipPayment: MembershipPaymentApi
    ): Single<MembershipPaymentApi>

    @PATCH("members/{memberId}")
    fun patchMember(@Header(AUTHORIZATION_HEADER) authorization: String,
                    @Path("memberId") memberId: UUID,
                    @Body patchParams: JsonObject): Single<MemberApi>

    @Multipart
    @PATCH("members/{memberId}")
    fun patchPhoto(@Header(AUTHORIZATION_HEADER) authorization: String,
                   @Path("memberId") memberId: UUID,
                   @Part("photo") photo: RequestBody): Single<MemberApi>

    @GET("administrative_divisions")
    fun getAdministrationDivisions(
        @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
        @Query("within_jurisdiction") withinJurisdiction: Boolean = true
    ): Single<List<AdministrativeDivisionApi>>

    @GET("enrollment_periods")
    fun getEnrollmentPeriods(
        @Header(AUTHORIZATION_HEADER) tokenAuthorization: String
    ): Single<List<EnrollmentPeriodApi>>
}

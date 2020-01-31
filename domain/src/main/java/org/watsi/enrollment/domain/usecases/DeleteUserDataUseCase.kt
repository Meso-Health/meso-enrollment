package org.watsi.enrollment.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.enrollment.domain.repositories.AdministrativeDivisionRepository
import org.watsi.enrollment.domain.repositories.DeltaRepository
import org.watsi.enrollment.domain.repositories.EnrollmentPeriodRepository
import org.watsi.enrollment.domain.repositories.HouseholdEnrollmentRecordRepository
import org.watsi.enrollment.domain.repositories.HouseholdRepository
import org.watsi.enrollment.domain.repositories.MemberEnrollmentRecordRepository
import org.watsi.enrollment.domain.repositories.MemberRepository
import org.watsi.enrollment.domain.repositories.MembershipPaymentRepository

class DeleteUserDataUseCase(
    private val deltaRepository: DeltaRepository,
    private val administrativeDivisionRepository: AdministrativeDivisionRepository,
    private val householdEnrollmentRecordRepository: HouseholdEnrollmentRecordRepository,
    private val householdRepository: HouseholdRepository,
    private val memberEnrollmentRecordRepository: MemberEnrollmentRecordRepository,
    private val memberRepository: MemberRepository,
    private val membershipPaymentRepository: MembershipPaymentRepository,
    private val enrollmentPeriodRepository: EnrollmentPeriodRepository
) {
    fun execute(): Completable {
        return Completable.concatArray(
            membershipPaymentRepository.deleteAll(),
            memberEnrollmentRecordRepository.deleteAll(),
            memberRepository.deleteAll(),
            householdEnrollmentRecordRepository.deleteAll(),
            householdRepository.deleteAll(),
            deltaRepository.deleteAll(),
            enrollmentPeriodRepository.deleteAll(),
            administrativeDivisionRepository.deleteAll()
        ).subscribeOn(Schedulers.io())
    }
}

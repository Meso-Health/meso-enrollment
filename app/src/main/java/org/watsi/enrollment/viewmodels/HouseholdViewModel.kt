package org.watsi.enrollment.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import io.reactivex.Flowable
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.entities.EnrollmentPeriod
import org.watsi.enrollment.domain.relations.HouseholdWithMembersAndPayments
import org.watsi.enrollment.domain.usecases.LoadCurrentEnrollmentPeriodUseCase
import org.watsi.enrollment.domain.usecases.LoadHouseholdUseCase
import java.util.UUID
import javax.inject.Inject

class HouseholdViewModel @Inject constructor(
        private val loadHouseholdUseCase: LoadHouseholdUseCase,
        private val loadEnrollmentPeriodUseCase: LoadCurrentEnrollmentPeriodUseCase,
        private val logger: Logger
) : ViewModel() {

    fun getObservable(householdId: UUID): LiveData<ViewState> {
        val householdFlowable = loadHouseholdUseCase.execute(householdId).doOnError {
            logger.error(it)
        }.map {
            // only expose unarchived members to HouseholdFragment
            it.copy(members = it.unarchivedAndUnpaidMembers())
        }

        val enrollmentPeriodFlowable = loadEnrollmentPeriodUseCase.executeFlowable().doOnError {
            logger.error(it)
        }

        return LiveDataReactiveStreams.fromPublisher(
            Flowable.combineLatest(
                listOf(householdFlowable, enrollmentPeriodFlowable),
                { result ->
                    ViewState(
                        result[0] as HouseholdWithMembersAndPayments?,
                        result[1] as EnrollmentPeriod?
                    )
                }
            ).onErrorReturn {
                ViewState(null, null)
            }
        )
    }

    data class ViewState(
        val householdWithMembers: HouseholdWithMembersAndPayments? = null,
        val enrollmentPeriod: EnrollmentPeriod?
    )
}

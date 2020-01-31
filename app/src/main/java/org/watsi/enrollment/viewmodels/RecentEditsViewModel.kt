package org.watsi.enrollment.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.enrollment.device.managers.Logger
import org.watsi.enrollment.domain.relations.HouseholdWithMembers
import org.watsi.enrollment.domain.usecases.LoadHouseholdsUseCase
import javax.inject.Inject

class RecentEditsViewModel @Inject constructor (
        private val loadHouseholdsUseCase: LoadHouseholdsUseCase,
        private val clock: Clock,
        private val logger: Logger
) : ViewModel() {

    companion object {
        const val DAYS_AGO_TO_SHOW_ENROLLED_HOUSEHOLDS = 7L
    }

    fun getObservable(): LiveData<ViewState> {

        val oneWeekAgo = LocalDate
                .now(clock.zone)
                .atStartOfDay(clock.zone)
                .minusDays(DAYS_AGO_TO_SHOW_ENROLLED_HOUSEHOLDS)
                .toInstant()

        return LiveDataReactiveStreams.fromPublisher(
            loadHouseholdsUseCase.execute(oneWeekAgo).map { result ->
                val households = result.filterNot {
                    // Exclude households where all members are archivedPermanently.
                    it.members.all {
                        memberWithThumbnail -> memberWithThumbnail.member.archivedPermanently()
                    }
                }.map { householdWithMembers ->
                    householdWithMembers.copy(members = householdWithMembers.members.filterNot { memberWithThumbnail ->
                        // Exclude members that are archivedPermanently
                        memberWithThumbnail.member.archivedPermanently()
                    })
                }

                ViewState(
                    households = households,
                    error = null
                )

            }.onErrorReturn {
                logger.error(it)
                ViewState(
                    households = emptyList(),
                    error = it.localizedMessage
                )
            }.startWith(
                ViewState(
                    households = emptyList(),
                    error = null
                )
            )
        )
    }

    data class ViewState(
        val households: List<HouseholdWithMembers>,
        val error: String?
    )
}

package org.watsi.enrollment.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.watsi.enrollment.fragments.EditMemberFragment
import org.watsi.enrollment.fragments.HomeFragment
import org.watsi.enrollment.fragments.HouseholdFragment
import org.watsi.enrollment.fragments.MemberNotFoundFragment
import org.watsi.enrollment.fragments.MemberSearchFragment
import org.watsi.enrollment.fragments.MemberSearchWithMembershipNumberFragment
import org.watsi.enrollment.fragments.NewHouseholdFragment
import org.watsi.enrollment.fragments.NewMemberFragment
import org.watsi.enrollment.fragments.PaymentFragment
import org.watsi.enrollment.fragments.RecentEditsFragment
import org.watsi.enrollment.fragments.ReviewFragment
import org.watsi.enrollment.fragments.StatsFragment
import org.watsi.enrollment.fragments.StatusFragment

@Module
abstract class FragmentModule {
    @ContributesAndroidInjector abstract fun bindHomeFragment(): HomeFragment
    @ContributesAndroidInjector abstract fun bindRecentEditsFragment(): RecentEditsFragment
    @ContributesAndroidInjector abstract fun bindHouseholdFragment(): HouseholdFragment
    @ContributesAndroidInjector abstract fun bindMemberSearchFragment(): MemberSearchFragment
    @ContributesAndroidInjector abstract fun bindNewMemberFragment(): NewMemberFragment
    @ContributesAndroidInjector abstract fun bindNewHouseholdFragment(): NewHouseholdFragment
    @ContributesAndroidInjector abstract fun bindStatusFragment(): StatusFragment
    @ContributesAndroidInjector abstract fun bindEditMemberFragment(): EditMemberFragment
    @ContributesAndroidInjector abstract fun bindSearchFragment(): MemberSearchWithMembershipNumberFragment
    @ContributesAndroidInjector abstract fun bindStatsFragment(): StatsFragment
    @ContributesAndroidInjector abstract fun bindReviewFragment(): ReviewFragment
    @ContributesAndroidInjector abstract fun bindPaymentFragment(): PaymentFragment
    @ContributesAndroidInjector abstract fun bindMemberNotFoundFragment(): MemberNotFoundFragment
}

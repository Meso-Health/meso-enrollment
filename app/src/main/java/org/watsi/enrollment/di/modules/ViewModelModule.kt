package org.watsi.enrollment.di.modules

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.watsi.enrollment.di.ViewModelKey
import org.watsi.enrollment.viewmodels.DaggerViewModelFactory
import org.watsi.enrollment.viewmodels.EditMemberViewModel
import org.watsi.enrollment.viewmodels.HouseholdViewModel
import org.watsi.enrollment.viewmodels.MemberSearchViewModel
import org.watsi.enrollment.viewmodels.NewHouseholdViewModel
import org.watsi.enrollment.viewmodels.NewMemberViewModel
import org.watsi.enrollment.viewmodels.PaymentViewModel
import org.watsi.enrollment.viewmodels.RecentEditsViewModel
import org.watsi.enrollment.viewmodels.StatsViewModel
import org.watsi.enrollment.viewmodels.StatusViewModel

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(NewMemberViewModel::class)
    abstract fun bindNewMemberViewModel(viewModel: NewMemberViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RecentEditsViewModel::class)
    abstract fun bindRecentEditsViewModel(viewModel: RecentEditsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HouseholdViewModel::class)
    abstract fun bindHouseholdViewModel(viewModel: HouseholdViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditMemberViewModel::class)
    abstract fun bindEditMemberViewModel(viewModel: EditMemberViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StatusViewModel::class)
    abstract fun bindStatusViewModel(viewModel: StatusViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NewHouseholdViewModel::class)
    abstract fun bindNewHouseholdViewModel(viewModel: NewHouseholdViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PaymentViewModel::class)
    abstract fun bindPaymentViewModel(viewModel: PaymentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MemberSearchViewModel::class)
    abstract fun bindMemberSearchViewModel(viewModel: MemberSearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StatsViewModel::class)
    abstract fun bindStatsViewModel(viewModel: StatsViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: DaggerViewModelFactory): ViewModelProvider.Factory
}

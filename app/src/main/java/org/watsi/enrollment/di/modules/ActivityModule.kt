package org.watsi.enrollment.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.watsi.enrollment.activities.AuthenticationActivity
import org.watsi.enrollment.activities.MainActivity
import org.watsi.enrollment.activities.ScanNewMemberCardActivity
import org.watsi.enrollment.activities.SavePhotoActivity
import org.watsi.enrollment.activities.SearchHouseholdByCardActivity
import org.watsi.enrollment.di.scopes.ActivityScope

@Module
abstract class ActivityModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentModule::class, MainActivityModule::class])
    abstract fun bindMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindAuthenticationActivity(): AuthenticationActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindSearchHouseholdByCardActivity(): SearchHouseholdByCardActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindScanNewMemberCardActivity(): ScanNewMemberCardActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindSavePhotoActivity(): SavePhotoActivity
}

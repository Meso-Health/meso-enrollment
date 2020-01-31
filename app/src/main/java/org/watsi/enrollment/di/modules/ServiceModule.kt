package org.watsi.enrollment.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.watsi.enrollment.services.FetchService
import org.watsi.enrollment.services.SyncDataService
import org.watsi.enrollment.services.SyncPhotosService

@Module
abstract class ServiceModule {
    @ContributesAndroidInjector
    abstract fun bindSyncEnrollmentRecordsService(): SyncDataService

    @ContributesAndroidInjector
    abstract fun bindSyncPhotosService(): SyncPhotosService

    @ContributesAndroidInjector
    abstract fun bindFetchService(): FetchService
}

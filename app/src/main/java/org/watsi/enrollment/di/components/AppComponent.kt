package org.watsi.enrollment.di.components

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import org.watsi.enrollment.BaseApplication
import org.watsi.enrollment.di.modules.ActivityModule
import org.watsi.enrollment.di.modules.ApiModule
import org.watsi.enrollment.di.modules.ApplicationBindingModule
import org.watsi.enrollment.di.modules.ClockModule
import org.watsi.enrollment.di.modules.DbModule
import org.watsi.enrollment.di.modules.DeviceModule
import org.watsi.enrollment.di.modules.RepositoryModule
import org.watsi.enrollment.di.modules.ServiceModule
import org.watsi.enrollment.di.modules.UseCaseModule
import org.watsi.enrollment.di.modules.ViewModelModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    ApplicationBindingModule::class,
    DeviceModule::class,
    DbModule::class,
    ApiModule::class,
    ServiceModule::class,
    ActivityModule::class,
    ViewModelModule::class,
    UseCaseModule::class,
    RepositoryModule::class,
    ClockModule::class
])
interface AppComponent : AndroidInjector<BaseApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<BaseApplication>()
}

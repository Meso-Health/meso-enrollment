package org.watsi.enrollment.di.modules

import dagger.Module
import dagger.Provides
import org.watsi.enrollment.R
import org.watsi.enrollment.activities.MainActivity
import org.watsi.enrollment.managers.NavigationManager

/**
 * For any resources that should be scoped to the MainActivity lifecycle
 */
@Module
class MainActivityModule {

    @Provides
    fun provideNavigationManager(activity: MainActivity): NavigationManager {
        return NavigationManager(activity.supportFragmentManager, R.id.fragment_container)
    }
}

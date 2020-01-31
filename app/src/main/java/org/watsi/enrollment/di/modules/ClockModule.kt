package org.watsi.enrollment.di.modules

import dagger.Module
import dagger.Provides
import org.threeten.bp.Clock

@Module
class ClockModule {

    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}

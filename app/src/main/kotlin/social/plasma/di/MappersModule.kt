package social.plasma.di

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import social.plasma.ui.RealStringManager
import social.plasma.ui.StringManager
import social.plasma.ui.util.InstantFormatter
import social.plasma.ui.util.RealInstantFormatter

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class MappersModule {
    @Binds
    abstract fun bindsInstantFormatter(impl: RealInstantFormatter): InstantFormatter

    @Binds
    abstract fun bindsStringManager(impl: RealStringManager) : StringManager

    companion object {
        @Provides
        fun providesContext(app: Application) : Context = app
    }
}

package social.plasma.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import social.plasma.ui.util.InstantFormatter
import social.plasma.ui.util.RealInstantFormatter

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class MappersModule {
    @Binds
    abstract fun bindsInstantFormatter(impl: RealInstantFormatter): InstantFormatter
}

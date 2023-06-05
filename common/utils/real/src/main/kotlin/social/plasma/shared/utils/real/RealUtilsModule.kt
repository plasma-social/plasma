package social.plasma.shared.utils.real

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import social.plasma.shared.utils.api.InstantFormatter
import social.plasma.shared.utils.api.NumberFormatter
import social.plasma.shared.utils.api.StringManager

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class RealUtilsModule {
    @Binds
    abstract fun bindsStringManager(impl: RealStringManager): StringManager

    @Binds
    abstract fun bindsInstantFormatter(impl: RealInstantFormatter): InstantFormatter

    @Binds
    abstract fun bindsNumberFormatter(impl: RealNumberFormatter): NumberFormatter

    companion object {
        @Provides
        fun providesContext(app: Application): Context = app
    }
}

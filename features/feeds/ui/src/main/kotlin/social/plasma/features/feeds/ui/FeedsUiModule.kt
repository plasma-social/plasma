package social.plasma.features.feeds.ui

import com.slack.circuit.runtime.ui.Ui
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class FeedsUiModule {
    @Binds
    @IntoSet
    abstract fun bindsUiFactory(impl: FeedsUiFactory): Ui.Factory
}

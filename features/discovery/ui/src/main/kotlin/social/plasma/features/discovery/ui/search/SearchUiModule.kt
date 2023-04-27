package social.plasma.features.discovery.ui.search

import com.slack.circuit.Ui
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import social.plasma.features.discovery.ui.DiscoveryUiFactory

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class SearchUiModule {
    @Binds
    @IntoSet
    abstract fun bindsUiFactory(impl: DiscoveryUiFactory): Ui.Factory
}

package social.plasma.features.discovery.presenters

import com.slack.circuit.runtime.presenter.Presenter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class DiscoveryPresentersModule {
    @Binds
    @IntoSet
    abstract fun bindsDiscoveryPresenterFactory(factory: DiscoveryPresentersFactory): Presenter.Factory
}


package social.plasma.feeds.presenters

import com.slack.circuit.Presenter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class FeedsPresentersModule {
    @Binds
    @IntoSet
    abstract fun bindsFeedsPresenterFactory(factory: FeedsPresentersFactory) : Presenter.Factory
}


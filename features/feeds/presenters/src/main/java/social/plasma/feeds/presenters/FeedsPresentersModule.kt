package social.plasma.feeds.presenters

import com.slack.circuit.runtime.presenter.Presenter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import social.plasma.feeds.presenters.feed.FeedStateProducer
import social.plasma.feeds.presenters.feed.RealFeedStateProducer

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class FeedsPresentersModule {
    @Binds
    @IntoSet
    abstract fun bindsFeedsPresenterFactory(factory: FeedsPresentersFactory): Presenter.Factory

    @Binds
    internal abstract fun bindsFeedUiProducer(producer: RealFeedStateProducer): FeedStateProducer
}


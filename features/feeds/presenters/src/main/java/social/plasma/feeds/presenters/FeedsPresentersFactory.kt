package social.plasma.feeds.presenters

import androidx.paging.PagingConfig
import com.slack.circuit.CircuitContext
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import com.slack.circuit.Screen
import kotlinx.coroutines.flow.onStart
import social.plasma.domain.observers.ObservePagedFollowingFeed
import social.plasma.domain.observers.ObservePagedNotificationsFeed
import social.plasma.domain.observers.ObservePagedRepliesFeed
import social.plasma.features.feeds.screens.threads.ThreadScreen
import social.plasma.features.feeds.screens.feed.FeedScreen
import social.plasma.features.feeds.screens.feed.FeedType
import social.plasma.features.feeds.screens.homefeeds.HomeFeeds
import social.plasma.feeds.presenters.feed.FeedPresenter
import social.plasma.feeds.presenters.thread.ThreadScreenPresenter
import javax.inject.Inject

class FeedsPresentersFactory @Inject constructor(
    private val homeFeedsPresenter: HomeFeedsPresenter.Factory,
    private val threadScreenPresenter: ThreadScreenPresenter.Factory,
    private val feedPresenter: FeedPresenter.Factory,
    private val observeFollowingFeed: ObservePagedFollowingFeed,
    private val observeNotificationsFeed: ObservePagedNotificationsFeed,
    private val observeRepliesFeed: ObservePagedRepliesFeed,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is ThreadScreen -> threadScreenPresenter.create(screen, navigator)
            HomeFeeds -> homeFeedsPresenter.create(navigator)
            is FeedScreen -> {
                val pagingConfig = PagingConfig(
                    pageSize = 20,
                )

                val pagingFlow = when (screen.feedType) {
                    FeedType.Following -> observeFollowingFeed.flow.onStart {
                        observeFollowingFeed(
                            ObservePagedFollowingFeed.Params(pagingConfig = pagingConfig)
                        )
                    }

                    FeedType.Replies -> observeRepliesFeed.flow.onStart {
                        observeRepliesFeed(
                            ObservePagedRepliesFeed.Params(pagingConfig = pagingConfig)
                        )
                    }

                    FeedType.Notifications -> observeNotificationsFeed.flow.onStart {
                        observeNotificationsFeed(
                            ObservePagedNotificationsFeed.Params(pagingConfig = pagingConfig)
                        )
                    }
                }
                feedPresenter.create(navigator, pagingFlow)
            }

            else -> null
        }
    }
}
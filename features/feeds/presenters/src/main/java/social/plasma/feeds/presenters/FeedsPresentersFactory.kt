package social.plasma.feeds.presenters

import com.slack.circuit.CircuitContext
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import com.slack.circuit.Screen
import social.plasma.features.feeds.screens.feed.FeedScreen
import social.plasma.features.feeds.screens.homefeeds.HomeFeeds
import social.plasma.features.feeds.screens.notifications.NotificationsFeedScreen
import social.plasma.features.feeds.screens.threads.HashTagFeedScreen
import social.plasma.features.feeds.screens.threads.ThreadScreen
import social.plasma.feeds.presenters.thread.HashTagScreenPresenter
import social.plasma.feeds.presenters.thread.ThreadScreenPresenter
import javax.inject.Inject

class FeedsPresentersFactory @Inject constructor(
    private val homeFeedsPresenter: HomeFeedsPresenter.Factory,
    private val threadScreenPresenter: ThreadScreenPresenter.Factory,
    private val hashTagScreenPresenter: HashTagScreenPresenter.Factory,
    private val notificationScreenPresenter: NotificationsFeedPresenter.Factory,
    private val feedScreenPresenter: FeedScreenPresenter.Factory,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is ThreadScreen -> threadScreenPresenter.create(screen, navigator)
            is HashTagFeedScreen -> hashTagScreenPresenter.create(screen, navigator)
            is NotificationsFeedScreen -> notificationScreenPresenter.create(navigator)
            HomeFeeds -> homeFeedsPresenter.create(navigator)
            is FeedScreen -> feedScreenPresenter.create(screen, navigator)
            else -> null
        }
    }
}

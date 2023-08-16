package social.plasma.feeds.presenters

import androidx.paging.PagingConfig
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.flow.onStart
import social.plasma.domain.observers.ObservePagedFollowingFeed
import social.plasma.domain.observers.ObservePagedNotificationsFeed
import social.plasma.domain.observers.ObservePagedRepliesFeed
import social.plasma.features.feeds.screens.feed.FeedScreen
import social.plasma.features.feeds.screens.feed.FeedType
import social.plasma.features.feeds.screens.feeditems.notes.NoteScreen
import social.plasma.features.feeds.screens.feeditems.quotednotes.QuotedNoteScreen
import social.plasma.features.feeds.screens.homefeeds.HomeFeeds
import social.plasma.features.feeds.screens.notifications.NotificationsFeedScreen
import social.plasma.features.feeds.screens.threads.HashTagFeedScreen
import social.plasma.features.feeds.screens.threads.ThreadScreen
import social.plasma.feeds.presenters.eventfeed.EventFeedPresenter
import social.plasma.feeds.presenters.hashtag.HashTagScreenPresenter
import social.plasma.feeds.presenters.notes.NotePresenter
import social.plasma.feeds.presenters.notes.QuotedNotePresenter
import social.plasma.feeds.presenters.thread.ThreadScreenPresenter
import javax.inject.Inject

class FeedsPresentersFactory @Inject constructor(
    private val homeFeedsPresenter: HomeFeedsPresenter.Factory,
    private val threadScreenPresenter: ThreadScreenPresenter.Factory,
    private val hashTagScreenPresenter: HashTagScreenPresenter.Factory,
    private val notificationScreenPresenter: NotificationsFeedPresenter.Factory,
    private val noteScreenPresenter: NotePresenter.Factory,
    private val quotedNotePresenter: QuotedNotePresenter.Factory,
    private val feedPresenter: EventFeedPresenter.Factory,
    private val observePagedFollowingFeed: ObservePagedFollowingFeed,
    private val observePagedNotificationsFeed: ObservePagedNotificationsFeed,
    private val observePagedRepliesFeed: ObservePagedRepliesFeed,
) : Presenter.Factory {

    private val config = PagingConfig(
        pageSize = 50,
        jumpThreshold = 20,
        maxSize = 1000,
    )

    private val followingFeed = observePagedFollowingFeed.flow.onStart {
        observePagedFollowingFeed(
            ObservePagedFollowingFeed.Params(config)
        )
    }

    private val repliesFeed = observePagedRepliesFeed.flow.onStart {
        observePagedRepliesFeed(
            ObservePagedRepliesFeed.Params(config)
        )
    }

    private val notificationsFeed = observePagedNotificationsFeed.flow.onStart {
        observePagedNotificationsFeed(
            ObservePagedNotificationsFeed.Params(config)
        )
    }

    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is ThreadScreen -> threadScreenPresenter.create(screen, navigator)
            is HashTagFeedScreen -> hashTagScreenPresenter.create(screen, navigator)
            is NotificationsFeedScreen -> notificationScreenPresenter.create(navigator)
            is QuotedNoteScreen -> quotedNotePresenter.create(screen, navigator)
            HomeFeeds -> homeFeedsPresenter.create(navigator)
            is FeedScreen -> when (screen.feedType) {
                FeedType.Following -> feedPresenter.create(navigator, followingFeed)
                FeedType.Replies -> feedPresenter.create(navigator, repliesFeed)
                FeedType.Notifications -> feedPresenter.create(navigator, notificationsFeed)
            }

            is NoteScreen -> noteScreenPresenter.create(screen, navigator)

            else -> null
        }
    }
}

package social.plasma.feeds.presenters

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import social.plasma.features.feeds.screens.feed.FeedScreen
import social.plasma.features.feeds.screens.feed.FeedType
import social.plasma.features.feeds.screens.homefeeds.HomeFeeds
import social.plasma.features.feeds.screens.notifications.NotificationsFeedScreen
import social.plasma.features.feeds.screens.threads.HashTagFeedScreen
import social.plasma.features.feeds.screens.threads.ThreadScreen
import social.plasma.feeds.presenters.feed.FeedPresenter
import social.plasma.feeds.presenters.feed.NotePagingFlowMapper
import social.plasma.feeds.presenters.thread.HashTagScreenPresenter
import social.plasma.feeds.presenters.thread.ThreadScreenPresenter
import social.plasma.shared.repositories.api.NoteRepository
import javax.inject.Inject

class FeedsPresentersFactory @Inject constructor(
    private val homeFeedsPresenter: HomeFeedsPresenter.Factory,
    private val threadScreenPresenter: ThreadScreenPresenter.Factory,
    private val hashTagScreenPresenter: HashTagScreenPresenter.Factory,
    private val notificationScreenPresenter: NotificationsFeedPresenter.Factory,
    private val noteRepository: NoteRepository,
    private val feedPresenter: FeedPresenter.Factory,
    notesMapper: NotePagingFlowMapper,
    scope: CoroutineScope,
) : Presenter.Factory {

    private val config = PagingConfig(
        pageSize = 20,
        jumpThreshold = 10,
        enablePlaceholders = true,
        prefetchDistance = 5,
    )

    private val followingFlow =
        notesMapper.map(
            Pager(
                config = config,
                pagingSourceFactory = noteRepository::observePagedContactsNotes,
            ).flow.distinctUntilChanged()
        ).cachedIn(scope)

    private val repliesFeed = notesMapper.map(
        Pager(
            config = config,
            pagingSourceFactory = noteRepository::observePagedContactsReplies,
        ).flow.distinctUntilChanged()
    ).cachedIn(scope)

    private val notificationsFeed = notesMapper.map(
        Pager(
            config = config,
            pagingSourceFactory = noteRepository::observePagedNotifications,
        ).flow.distinctUntilChanged()
    ).cachedIn(scope)

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
            is FeedScreen -> when (screen.feedType) {
                FeedType.Following -> feedPresenter.create(navigator, followingFlow)
                FeedType.Replies -> feedPresenter.create(navigator, repliesFeed)
                FeedType.Notifications -> feedPresenter.create(navigator, notificationsFeed)
            }

            else -> null
        }
    }
}

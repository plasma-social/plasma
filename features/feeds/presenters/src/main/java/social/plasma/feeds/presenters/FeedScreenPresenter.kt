package social.plasma.feeds.presenters

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.paging.PagingConfig
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import com.slack.circuit.retained.rememberRetained
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import social.plasma.domain.observers.ObservePagedFollowingFeed
import social.plasma.domain.observers.ObservePagedNotificationsFeed
import social.plasma.domain.observers.ObservePagedRepliesFeed
import social.plasma.features.feeds.screens.feed.FeedScreen
import social.plasma.features.feeds.screens.feed.FeedType
import social.plasma.features.feeds.screens.feed.FeedUiState
import social.plasma.feeds.presenters.feed.FeedPresenter
import java.util.UUID

class FeedScreenPresenter @AssistedInject constructor(
    private val feedPresenterFactory: FeedPresenter.Factory,
    private val observeFollowingFeed: ObservePagedFollowingFeed,
    private val observeNotificationsFeed: ObservePagedNotificationsFeed,
    private val observeRepliesFeed: ObservePagedRepliesFeed,
    @Assisted private val screen: FeedScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<FeedUiState> {

//    private val feedPresenter = feedPresenterFactory.create(navigator, pagingFlow)

    @Composable
    override fun present(): FeedUiState {
        val uuid = rememberRetained { UUID.randomUUID().toString() }
        Log.d("@@@", "feedPresenter uuid = ${uuid}")
        val feedPresenter = rememberRetained(key = screen.feedType.name) {
            val pagingConfig = PagingConfig(
                pageSize = 20,
                prefetchDistance = 10,
                initialLoadSize = 50,
                jumpThreshold = 10,
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
            }.distinctUntilChanged()


            feedPresenterFactory.create(navigator, pagingFlow)
        }
        Log.d("@@@", "feedPresenter object = ${feedPresenter}")
        return feedPresenter.present()
    }

    @AssistedFactory
    interface Factory {
        fun create(args: FeedScreen, navigator: Navigator): FeedScreenPresenter
    }
}

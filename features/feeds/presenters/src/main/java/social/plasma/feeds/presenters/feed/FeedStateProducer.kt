package social.plasma.feeds.presenters.feed

import androidx.compose.runtime.Composable
import androidx.paging.PagingData
import com.slack.circuit.runtime.Navigator
import kotlinx.coroutines.flow.Flow
import social.plasma.features.feeds.screens.eventfeed.EventFeedUiState
import social.plasma.features.feeds.screens.feed.FeedUiState
import social.plasma.models.EventModel

/**
 * A producer of [FeedUiState].
 *
 * This abstraction allows us to test presenters that depend
 * on [FeedUiState] without having to provide all the fakes for feed ui presenter.
 */
fun interface FeedStateProducer {
    @Composable
    operator fun invoke(
        navigator: Navigator,
        pagingFlow: Flow<PagingData<EventModel>>,
    ): EventFeedUiState
}

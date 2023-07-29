package social.plasma.feeds.presenters.feed

import androidx.compose.runtime.Composable
import androidx.paging.PagingData
import com.slack.circuit.runtime.Navigator
import kotlinx.coroutines.flow.Flow
import social.plasma.features.feeds.screens.eventfeed.EventFeedUiState
import social.plasma.feeds.presenters.eventfeed.EventFeedPresenter
import social.plasma.models.EventModel
import javax.inject.Inject

internal class RealFeedStateProducer @Inject constructor(
    private val feedPresenterFactory: EventFeedPresenter.Factory,
) : FeedStateProducer {
    @Composable
    override fun invoke(
        navigator: Navigator,
        pagingFlow: Flow<PagingData<EventModel>>,
    ): EventFeedUiState = feedPresenterFactory.create(navigator, pagingFlow).present()
}

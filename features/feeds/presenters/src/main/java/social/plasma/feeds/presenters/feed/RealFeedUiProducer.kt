package social.plasma.feeds.presenters.feed

import androidx.compose.runtime.Composable
import androidx.paging.PagingData
import com.slack.circuit.runtime.Navigator
import kotlinx.coroutines.flow.Flow
import social.plasma.features.feeds.screens.feed.FeedUiState
import social.plasma.models.NoteWithUser
import javax.inject.Inject

internal class RealFeedUiProducer @Inject constructor(
    private val feedPresenterFactory: FeedPresenter.Factory,
) : FeedUiProducer {
    @Composable
    override fun invoke(
        navigator: Navigator,
        pagingFlow: Flow<PagingData<NoteWithUser>>,
    ): FeedUiState = feedPresenterFactory.create(navigator, pagingFlow).present()
}

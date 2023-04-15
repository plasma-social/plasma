package social.plasma.features.feeds.screens.hashtags

import androidx.paging.PagingData
import com.slack.circuit.CircuitUiState
import kotlinx.coroutines.flow.Flow
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.features.feeds.screens.feed.FeedUiState
import social.plasma.opengraph.OpenGraphMetadata

data class HashTagScreenUiState(
    val title: String,
    val pagingFlow: Flow<PagingData<FeedItem>>,
    val getOpenGraphMetadata: suspend (String) -> OpenGraphMetadata?,
    val feedState: FeedUiState,
    val onEvent: (HashTagScreenUiEvent) -> Unit,
) : CircuitUiState {
}

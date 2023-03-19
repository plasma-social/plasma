package social.plasma.features.feeds.screens.threads

import androidx.paging.PagingData
import com.slack.circuit.CircuitUiState
import kotlinx.coroutines.flow.Flow
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.features.feeds.screens.feed.FeedUiEvent
import social.plasma.models.PubKey
import social.plasma.opengraph.OpenGraphMetadata

data class ThreadScreenUiState(
    val title: String,
    val pagingFlow: Flow<PagingData<ThreadItem>>,
    val getOpenGraphMetadata: suspend (String) -> OpenGraphMetadata?,
    val onEvent: (ThreadScreenUiEvent) -> Unit,
) : CircuitUiState {
}

sealed interface ThreadItem {
    val id: String
    val pubkey: PubKey

    data class RootNote(
        val noteUiModel: FeedItem.NoteCard,
        override val id: String = noteUiModel.id,
        override val pubkey: PubKey = noteUiModel.userPubkey,
    ) : ThreadItem

    data class LeafNote(
        val noteUiModel: FeedItem.NoteCard,
        override val id: String = noteUiModel.id,
        override val pubkey: PubKey = noteUiModel.userPubkey,
        val showConnector: Boolean = true,
    ) : ThreadItem
}
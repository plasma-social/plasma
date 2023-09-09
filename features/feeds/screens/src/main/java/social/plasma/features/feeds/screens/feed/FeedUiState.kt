package social.plasma.features.feeds.screens.feed

import androidx.compose.foundation.lazy.LazyListState
import androidx.paging.PagingData
import app.cash.nostrino.crypto.PubKey
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.coroutines.flow.Flow
import social.plasma.models.Mention
import social.plasma.models.Nip5Status
import social.plasma.models.NoteId
import social.plasma.models.TipAddress
import social.plasma.opengraph.OpenGraphMetadata

data class FeedUiState(
    val pagingFlow: Flow<PagingData<FeedItem>>,
    val getOpenGraphMetadata: suspend (String) -> OpenGraphMetadata?,
    val refreshText: String = "",
    val listState: LazyListState,
    val displayRefreshButton: Boolean = false,
    val onEvent: (FeedUiEvent) -> Unit,
) : CircuitUiState

sealed interface FeedItem {
    val key: String

    data class NoteCard(
        val id: String,
        override val key: String = id,
        val name: String = "",
        val displayName: String = "",
        val avatarUrl: String? = null,
        val nip5Identifier: String? = null,
        val headerContent: ContentBlock.Text? = null,
        val content: List<ContentBlock> = emptyList(),
        val cardLabel: String? = null,
        val timePosted: String = "",
        val replyCount: String = "",
        val shareCount: String = "",
        val likeCount: Int = 0,
        val userPubkey: PubKey,
        val hidden: Boolean = false,
        val isLiked: Boolean = false,
        val nip5Status: Nip5Status = Nip5Status.Missing,
        val zapsEnabled: Boolean = false,
        val tipAddress: TipAddress? = null,
    ) : FeedItem
}

sealed interface ContentBlock {

    data class Image(val imageUrl: String) : ContentBlock

    data class Video(val videoUrl: String) : ContentBlock

    data class Audio(val audioUrl: String, val waveform: List<Int>?) : ContentBlock

    data class Carousel(val imageUrls: List<String>) : ContentBlock

    data class Text(val content: String, val mentions: Map<Int, Mention>) : ContentBlock

    data class UrlPreview(val url: String) : ContentBlock
    data class NoteQuote(val noteId: NoteId) : ContentBlock
}

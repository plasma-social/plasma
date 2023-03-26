package social.plasma.features.feeds.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.slack.circuit.Ui
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.features.feeds.screens.feed.FeedUiEvent.OnNoteClick
import social.plasma.features.feeds.screens.feed.FeedUiEvent.OnNoteDisplayed
import social.plasma.features.feeds.screens.feed.FeedUiEvent.OnNoteReaction
import social.plasma.features.feeds.screens.feed.FeedUiEvent.OnNoteRepost
import social.plasma.features.feeds.screens.feed.FeedUiEvent.OnProfileClick
import social.plasma.features.feeds.screens.feed.FeedUiEvent.OnReplyClick
import social.plasma.features.feeds.screens.feed.FeedUiState
import social.plasma.features.feeds.ui.notes.NoteElevatedCard
import social.plasma.models.NoteId
import javax.inject.Inject

class FeedUi @Inject constructor() : Ui<FeedUiState> {
    @Composable
    override fun Content(state: FeedUiState, modifier: Modifier) {
        ListContent(state, modifier)
    }

    @Composable
    fun ListContent(
        state: FeedUiState,
        modifier: Modifier,
        contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
        headerContent: LazyListScope.() -> Unit = {},
    ) {
        val onEvent = state.onEvent
        val getOpenGraphMetadata = state.getOpenGraphMetadata
        val pagingLazyItems = state.pagingFlow.collectAsLazyPagingItems()

        Box(
            modifier = modifier.fillMaxSize()
        ) {

            if (pagingLazyItems.itemCount == 0) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
            ) {

                headerContent()

                items(pagingLazyItems, key = { it.key }) { item ->
                    when (item) {
                        is FeedItem.NoteCard -> {
                            if (!item.hidden) {
                                val noteId = NoteId(item.id)
                                NoteElevatedCard(
                                    uiModel = item,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .clickable {
                                            onEvent(OnNoteClick(noteId))
                                        },
                                    onAvatarClick = { onEvent(OnProfileClick(item.userPubkey)) },
                                    onLikeClick = { onEvent(OnNoteReaction(noteId)) },
                                    onReplyClick = { onEvent(OnReplyClick(noteId)) },
                                    onProfileClick = { onEvent(OnProfileClick(it)) },
                                    onNoteClick = { onEvent(OnNoteClick(it)) },
                                    onRepostClick = { onEvent(OnNoteRepost(noteId)) },
                                    getOpenGraphMetadata = getOpenGraphMetadata,
                                )
                                LaunchedEffect(Unit) {
                                    onEvent(OnNoteDisplayed(noteId, item.userPubkey))
                                }
                            }
                        }

                        null -> {}
                    }
                }
            }
        }

    }
}
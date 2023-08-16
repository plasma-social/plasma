package social.plasma.features.feeds.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.runtime.ui.Ui
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import social.plasma.features.feeds.screens.feed.FeedUiEvent
import social.plasma.features.feeds.screens.feed.FeedUiEvent.OnHashTagClick
import social.plasma.features.feeds.screens.feed.FeedUiEvent.OnNavEvent
import social.plasma.features.feeds.screens.feed.FeedUiEvent.OnNoteClick
import social.plasma.features.feeds.screens.feed.FeedUiEvent.OnNoteDisplayed
import social.plasma.features.feeds.screens.feed.FeedUiEvent.OnNoteReaction
import social.plasma.features.feeds.screens.feed.FeedUiEvent.OnNoteRepost
import social.plasma.features.feeds.screens.feed.FeedUiEvent.OnProfileClick
import social.plasma.features.feeds.screens.feed.FeedUiEvent.OnReplyClick
import social.plasma.features.feeds.screens.feed.FeedUiEvent.OnZapClick
import social.plasma.features.feeds.screens.threads.ThreadItem
import social.plasma.features.feeds.screens.threads.ThreadScreenUiEvent
import social.plasma.features.feeds.screens.threads.ThreadScreenUiEvent.OnFeedEvent
import social.plasma.features.feeds.screens.threads.ThreadScreenUiState
import social.plasma.features.feeds.ui.notes.NoteFlatCard
import social.plasma.features.feeds.ui.notes.ThreadNote
import social.plasma.models.NoteId
import social.plasma.ui.R
import social.plasma.ui.overlays.getZapAmount
import social.plasma.ui.rememberStableCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
class ThreadScreenUi : Ui<ThreadScreenUiState> {
    @Composable
    override fun Content(state: ThreadScreenUiState, modifier: Modifier) {
        val onScreenEvent = state.onEvent
        val getOpenGraphMetadata = state.getOpenGraphMetadata
        val overlayHost = LocalOverlayHost.current

        val onFeedItemEvent: (FeedUiEvent) -> Unit =
            remember(onScreenEvent) { { onScreenEvent(OnFeedEvent(it)) } }

        val pagingLazyItems = state.pagingFlow.collectAsLazyPagingItems()

        val isLoading by produceState(initialValue = false, pagingLazyItems.itemCount) {
            delay(300)
            value = pagingLazyItems.itemCount == 0
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(state.title) },
                    navigationIcon = {
                        IconButton(onClick = { onScreenEvent(ThreadScreenUiEvent.OnBackClick) }) {
                            Icon(
                                painterResource(R.drawable.ic_chevron_back),
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
        ) { paddingValue ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValue)
            ) {
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart)
                    )
                }
                val listState = rememberLazyListState()
                var rootIndex by remember { mutableStateOf(Int.MAX_VALUE) }

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {

                    items(
                        pagingLazyItems.itemCount,
                        key = pagingLazyItems.itemKey { it.id }) { index ->
                        val coroutineScope = rememberStableCoroutineScope()

                        when (val item = pagingLazyItems[index]) {
                            is ThreadItem.RootNote -> Column {
                                val noteId = remember { NoteId(item.id) }
                                rootIndex = index
                                NoteFlatCard(
                                    uiModel = item.noteUiModel,
                                    onAvatarClick = { onFeedItemEvent(OnProfileClick(item.pubkey)) },
                                    onLikeClick = { onFeedItemEvent(OnNoteReaction(noteId)) },
                                    onReplyClick = { onFeedItemEvent(OnReplyClick(noteId)) },
                                    onProfileClick = { onFeedItemEvent(OnProfileClick(it)) },
                                    onNoteClick = { onFeedItemEvent(OnNoteClick(it)) },
                                    onRepostClick = { onFeedItemEvent(OnNoteRepost(noteId)) },
                                    getOpenGraphMetadata = getOpenGraphMetadata,
                                    onHashTagClick = { onFeedItemEvent(OnHashTagClick(it)) },
                                    onNestedNavEvent = { onFeedItemEvent(OnNavEvent(it)) },
                                    onZapClick = {
                                        coroutineScope.launch {
                                            onFeedItemEvent(
                                                OnZapClick(
                                                    tipAddress = item.noteUiModel.tipAddress,
                                                    satAmount = overlayHost.getZapAmount(),
                                                    noteId = noteId,
                                                    pubKey = item.pubkey,
                                                )
                                            )
                                        }
                                    },
                                )
                                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                                Spacer(Modifier.height(32.dp))

                                LaunchedEffect(Unit) {
                                    onFeedItemEvent(
                                        OnNoteDisplayed(
                                            noteId,
                                            item.pubkey
                                        )
                                    )
                                }
                            }

                            is ThreadItem.LeafNote -> {
                                val noteId = remember { NoteId(item.id) }

                                val showConnector = index < rootIndex
//                                val showConnector by produceState(
//                                    initialValue = false,
//                                    index,
//                                    rootIndex
//                                ) {
//                                    value = index < rootIndex
//                                }

                                ThreadNote(
                                    uiModel = item.noteUiModel,
                                    modifier = Modifier.clickable {
                                        onFeedItemEvent(
                                            OnNoteClick(
                                                noteId
                                            )
                                        )
                                    },
                                    onAvatarClick = { onFeedItemEvent(OnProfileClick(item.pubkey)) },
                                    onLikeClick = { onFeedItemEvent(OnNoteReaction(noteId)) },
                                    onReplyClick = { onFeedItemEvent(OnReplyClick(noteId)) },
                                    showConnector = showConnector,
                                    onProfileClick = { onFeedItemEvent(OnProfileClick(it)) },
                                    onNoteClick = { onFeedItemEvent(OnNoteClick(it)) },
                                    onRepostClick = { onFeedItemEvent(OnNoteRepost(noteId)) },
                                    getOpenGraphMetadata = getOpenGraphMetadata,
                                    onHashTagClick = { onFeedItemEvent(OnHashTagClick(it)) },
                                    onNestedNavEvent = { onFeedItemEvent(OnNavEvent(it)) },
                                    onZapClick = {
                                        coroutineScope.launch {
                                            onFeedItemEvent(
                                                OnZapClick(
                                                    tipAddress = item.noteUiModel.tipAddress,
                                                    satAmount = overlayHost.getZapAmount(),
                                                    noteId = noteId,
                                                    pubKey = item.pubkey,
                                                )
                                            )
                                        }
                                    }
                                )
                                LaunchedEffect(Unit) {
                                    onFeedItemEvent(
                                        OnNoteDisplayed(
                                            noteId,
                                            item.pubkey
                                        )
                                    )
                                }
                            }

                            null -> {}
                        }
                    }
                }

            }
        }

    }
}

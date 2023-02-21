package social.plasma.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import kotlinx.coroutines.flow.Flow
import social.plasma.R
import social.plasma.models.NoteId
import social.plasma.models.PubKey
import social.plasma.ui.components.ProgressIndicator
import social.plasma.ui.components.notes.GetOpenGraphMetadata
import social.plasma.ui.components.notes.NoteElevatedCard
import social.plasma.ui.components.notes.NoteUiModel
import social.plasma.ui.feed.FeedUiEvent.OnNoteDisplayed
import social.plasma.ui.feed.FeedUiEvent.OnNoteDisposed
import social.plasma.ui.feed.FeedUiEvent.OnNoteReaction

@Composable
fun GlobalFeed(
    modifier: Modifier = Modifier,
    viewModel: GlobalFeedViewModel = hiltViewModel(),
    onNavigateToProfile: (PubKey) -> Unit,
    navigateToThread: (NoteId) -> Unit,
    onAddNote: () -> Unit,
    onNavigateToReply: (NoteId) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    FeedContent(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToProfile = onNavigateToProfile,
        onNoteClicked = navigateToThread,
        onAddNote = onAddNote,
        onReply = onNavigateToReply,
        getOpenGraphMetadata = viewModel::getOpenGraphMetadata,
    )
}

@Composable
fun RepliesFeed(
    modifier: Modifier = Modifier,
    viewModel: RepliesFeedViewModel = hiltViewModel(),
    onNavigateToProfile: (PubKey) -> Unit,
    navigateToThread: (NoteId) -> Unit,
    onAddNote: () -> Unit,
    onNavigateToReply: (NoteId) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    FeedContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateToProfile = onNavigateToProfile,
        onNoteClicked = navigateToThread,
        onAddNote = onAddNote,
        onReply = onNavigateToReply,
        getOpenGraphMetadata = viewModel::getOpenGraphMetadata,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun ContactsFeed(
    modifier: Modifier = Modifier,
    viewModel: FollowingFeedViewModel = hiltViewModel(),
    onNavigateToProfile: (PubKey) -> Unit,
    navigateToThread: (NoteId) -> Unit,
    onAddNote: () -> Unit,
    onNavigateToReply: (NoteId) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    FeedContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateToProfile = onNavigateToProfile,
        onNoteClicked = navigateToThread,
        onAddNote = onAddNote,
        onReply = onNavigateToReply,
        getOpenGraphMetadata = viewModel::getOpenGraphMetadata,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun FeedContent(
    modifier: Modifier = Modifier,
    uiState: FeedUiState,
    onNavigateToProfile: (PubKey) -> Unit,
    onNoteClicked: (NoteId) -> Unit,
    onAddNote: () -> Unit,
    onReply: (NoteId) -> Unit,
    getOpenGraphMetadata: GetOpenGraphMetadata,
    onEvent: (FeedUiEvent) -> Unit,
) {
    when (uiState) {
        is FeedUiState.Loading -> ProgressIndicator(modifier = modifier)
        is FeedUiState.Loaded -> FeedList(
            modifier = modifier,
            noteList = uiState.feedPagingFlow,
            showPostButton = uiState.showPostButton,
            onNavigateToProfile = onNavigateToProfile,
            onEvent = onEvent,
            onNoteClicked = onNoteClicked,
            onAddNote = onAddNote,
            onReply = onReply,
            getOpenGraphMetadata = getOpenGraphMetadata,
        )
    }
}

@Composable
private fun FeedList(
    noteList: Flow<PagingData<NoteUiModel>>,
    showPostButton: Boolean,
    modifier: Modifier = Modifier,
    onNavigateToProfile: (PubKey) -> Unit,
    onNoteClicked: (NoteId) -> Unit,
    onAddNote: () -> Unit,
    onReply: (NoteId) -> Unit,
    getOpenGraphMetadata: GetOpenGraphMetadata,
    onEvent: (FeedUiEvent) -> Unit,
) {
    val pagingLazyItems = noteList.collectAsLazyPagingItems()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {

            items(pagingLazyItems, key = { it.id }) { note ->
                note?.let {
                    val noteId = NoteId(note.id)

                    LaunchedEffect(Unit) {
                        onEvent(OnNoteDisplayed(noteId, note.userPubkey))
                    }
                    NoteElevatedCard(
                        uiModel = it,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable {
                                onNoteClicked(noteId)
                            },
                        onAvatarClick = { onNavigateToProfile(note.userPubkey) },
                        onLikeClick = { onEvent(OnNoteReaction(noteId)) },
                        onReplyClick = { onReply(noteId) },
                        getOpenGraphMetadata = getOpenGraphMetadata,
                        onProfileClick = onNavigateToProfile,
                        onNoteClick = onNoteClicked,
                    )

                    DisposableEffect(Unit) {
                        onDispose {
                            onEvent(OnNoteDisposed(noteId, note.userPubkey))
                        }
                    }
                }
            }
        }
        if (pagingLazyItems.itemCount == 0) {
            // TODO move to the viewmodel
            CircularProgressIndicator()
        }
        if (showPostButton) {
            FloatingActionButton(
                modifier = Modifier
                    .align(alignment = Alignment.BottomEnd)
                    .padding(all = 16.dp),
                onClick = onAddNote,
                shape = CircleShape,
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_plus),
                    contentDescription = stringResource(id = R.string.post_note)
                )
            }
        }
    }
}

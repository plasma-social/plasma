package social.plasma.ui.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import kotlinx.coroutines.flow.Flow
import social.plasma.PubKey
import social.plasma.ui.components.NoteCard
import social.plasma.ui.components.NoteCardUiModel
import social.plasma.ui.components.ProgressIndicator

typealias NoteId = String

@Composable
fun GlobalFeed(
    modifier: Modifier = Modifier,
    viewModel: GlobalFeedViewModel = hiltViewModel(),
    onNavigateToProfile: (PubKey) -> Unit,
) {
    val uiState by viewModel.uiState().collectAsState()

    FeedContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateToProfile = onNavigateToProfile,
        onNoteDisposed = viewModel::onNoteDisposed,
        onNoteDisplayed = viewModel::onNoteDisplayed,
    )
}

@Composable
fun ContactsFeed(
    modifier: Modifier = Modifier,
    viewModel: FollowingFeedViewModel = hiltViewModel(),
    onNavigateToProfile: (PubKey) -> Unit,
) {
    val uiState by viewModel.uiState().collectAsState()

    FeedContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateToProfile = onNavigateToProfile,
        onNoteDisposed = viewModel::onNoteDisposed,
        onNoteDisplayed = viewModel::onNoteDisplayed,
    )
}

@Composable
private fun FeedContent(
    modifier: Modifier = Modifier,
    uiState: FeedUiState,
    onNavigateToProfile: (PubKey) -> Unit,
    onNoteDisposed: (String) -> Unit,
    onNoteDisplayed: (NoteId, PubKey) -> Unit,
) {
    when (uiState) {
        is FeedUiState.Loading -> ProgressIndicator(modifier = modifier)
        is FeedUiState.Loaded -> FeedList(
            modifier = modifier,
            noteList = uiState.feedPagingFlow,
            onNavigateToProfile = onNavigateToProfile,
            onNoteDisplayed = onNoteDisplayed,
            onNoteDisposed = onNoteDisposed,
        )
    }
}

@Composable
private fun FeedList(
    noteList: Flow<PagingData<NoteCardUiModel>>,
    modifier: Modifier = Modifier,
    onNavigateToProfile: (PubKey) -> Unit,
    onNoteDisplayed: (NoteId, PubKey) -> Unit,
    onNoteDisposed: (String) -> Unit,
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

            items(pagingLazyItems) { note ->
                note?.let {
                    LaunchedEffect(Unit) {
                        onNoteDisplayed(note.id, note.userPubkey)
                    }
                    NoteCard(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        uiModel = it,
                        onAvatarClick = { onNavigateToProfile(note.userPubkey) },
                    )

                    DisposableEffect(Unit) {
                        onDispose {
                            onNoteDisposed(note.id)
                        }
                    }
                }
            }
        }
        if (pagingLazyItems.itemCount == 0) {
            // TODO move to the viewmodel
            CircularProgressIndicator()
        }
    }
}

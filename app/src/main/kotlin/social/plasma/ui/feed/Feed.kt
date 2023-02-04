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
import social.plasma.PubKey
import social.plasma.R
import social.plasma.ui.components.ProgressIndicator
import social.plasma.ui.components.notes.NoteElevatedCard
import social.plasma.ui.components.notes.NoteUiModel

typealias NoteId = String

@Composable
fun GlobalFeed(
    modifier: Modifier = Modifier,
    viewModel: GlobalFeedViewModel = hiltViewModel(),
    onNavigateToProfile: (PubKey) -> Unit,
    navigateToThread: (NoteId) -> Unit,
    onAddNote: () -> Unit,
) {
    val uiState by viewModel.uiState().collectAsState()

    FeedContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateToProfile = onNavigateToProfile,
        onNoteDisposed = viewModel::onNoteDisposed,
        onNoteDisplayed = viewModel::onNoteDisplayed,
        onNoteClicked = navigateToThread,
        onAddNote = onAddNote,
    )
}

@Composable
fun RepliesFeed(
    modifier: Modifier = Modifier,
    viewModel: RepliesFeedViewModel = hiltViewModel(),
    onNavigateToProfile: (PubKey) -> Unit,
    navigateToThread: (NoteId) -> Unit,
    onAddNote: () -> Unit,
) {
    val uiState by viewModel.uiState().collectAsState()

    FeedContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateToProfile = onNavigateToProfile,
        onNoteDisposed = viewModel::onNoteDisposed,
        onNoteDisplayed = viewModel::onNoteDisplayed,
        onNoteClicked = navigateToThread,
        onAddNote = onAddNote,
    )
}

@Composable
fun ContactsFeed(
    modifier: Modifier = Modifier,
    viewModel: FollowingFeedViewModel = hiltViewModel(),
    onNavigateToProfile: (PubKey) -> Unit,
    navigateToThread: (NoteId) -> Unit,
    onAddNote: () -> Unit,
) {
    val uiState by viewModel.uiState().collectAsState()

    FeedContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateToProfile = onNavigateToProfile,
        onNoteDisposed = viewModel::onNoteDisposed,
        onNoteDisplayed = viewModel::onNoteDisplayed,
        onNoteClicked = navigateToThread,
        onAddNote = onAddNote,
    )
}

@Composable
fun FeedContent(
    modifier: Modifier = Modifier,
    uiState: FeedUiState,
    onNavigateToProfile: (PubKey) -> Unit,
    onNoteDisposed: (NoteId, PubKey) -> Unit,
    onNoteDisplayed: (NoteId, PubKey) -> Unit,
    onNoteClicked: (NoteId) -> Unit,
    onAddNote: () -> Unit,
) {
    when (uiState) {
        is FeedUiState.Loading -> ProgressIndicator(modifier = modifier)
        is FeedUiState.Loaded -> FeedList(
            modifier = modifier,
            noteList = uiState.feedPagingFlow,
            onNavigateToProfile = onNavigateToProfile,
            onNoteDisplayed = onNoteDisplayed,
            onNoteDisposed = onNoteDisposed,
            onNoteClicked = onNoteClicked,
            onAddNote = onAddNote,
        )
    }
}

@Composable
private fun FeedList(
    noteList: Flow<PagingData<NoteUiModel>>,
    modifier: Modifier = Modifier,
    onNavigateToProfile: (PubKey) -> Unit,
    onNoteDisplayed: (NoteId, PubKey) -> Unit,
    onNoteDisposed: (NoteId, PubKey) -> Unit,
    onNoteClicked: (NoteId) -> Unit,
    onAddNote: () -> Unit,
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
                    LaunchedEffect(Unit) {
                        onNoteDisplayed(note.id, note.userPubkey)
                    }
                    NoteElevatedCard(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable {
                                onNoteClicked(it.id)
                            },
                        uiModel = it,
                        onAvatarClick = { onNavigateToProfile(note.userPubkey) },
                    )

                    DisposableEffect(Unit) {
                        onDispose {
                            onNoteDisposed(note.id, note.userPubkey)
                        }
                    }
                }
            }
        }
        if (pagingLazyItems.itemCount == 0) {
            // TODO move to the viewmodel
            CircularProgressIndicator()
        }
        FloatingActionButton(
            modifier = Modifier.align(alignment = Alignment.BottomEnd)
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

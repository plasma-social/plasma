package social.plasma.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import social.plasma.models.NoteId
import social.plasma.models.PubKey
import social.plasma.ui.components.notes.GetOpenGraphMetadata
import social.plasma.ui.components.notes.NoteFlatCard
import social.plasma.ui.components.notes.ThreadNote
import social.plasma.ui.theme.PlasmaTheme
import social.plasma.ui.threads.ThreadListViewModel
import social.plasma.ui.threads.ThreadNoteUiModel.LeafNote
import social.plasma.ui.threads.ThreadNoteUiModel.RootNote
import social.plasma.ui.threads.ThreadUiState
import kotlin.math.roundToInt


@Composable
fun ThreadList(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ThreadListViewModel = hiltViewModel(),
    onNavigateToThread: (NoteId) -> Unit,
    onNavigateToProfile: (PubKey) -> Unit,
    onNavigateToReply: (NoteId) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    ThreadList(
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        uiState = uiState,
        onNavigateToThread = onNavigateToThread,
        onNoteDisplayed = viewModel::onNoteDisplayed,
        onNoteDisposed = viewModel::onNoteDisposed,
        onProfileClick = onNavigateToProfile,
        onNoteReaction = viewModel::onNoteReaction,
        onReply = onNavigateToReply,
        getOpenGraphMetadata = viewModel::getOpenGraphMetadata
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThreadList(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    uiState: ThreadUiState,
    onNavigateToThread: (NoteId) -> Unit,
    onNoteDisplayed: (NoteId, PubKey) -> Unit,
    onNoteDisposed: (NoteId, PubKey) -> Unit,
    onProfileClick: (PubKey) -> Unit,
    onNoteReaction: (NoteId) -> Unit,
    onReply: (NoteId) -> Unit,
    getOpenGraphMetadata: GetOpenGraphMetadata,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(left = 0, right = 0, top = 0, bottom = 0),
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Thread") }, navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ChevronLeft, null)
                }
            })
        },
    ) {
        val state = rememberLazyListState(initialFirstVisibleItemIndex = uiState.firstVisibleItem)

        val scrollOffset = with(LocalDensity.current) { -16.dp.toPx().roundToInt() }

        LaunchedEffect(uiState.firstVisibleItem) {
            state.scrollToItem(uiState.firstVisibleItem, scrollOffset = scrollOffset)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            state = state,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(uiState.noteUiModels) { note ->
                val noteId = NoteId(note.id)

                when (note) {
                    is RootNote -> Column {
                        NoteFlatCard(
                            uiModel = note.noteUiModel,
                            onAvatarClick = { onProfileClick(note.pubkey) },
                            onLikeClick = { onNoteReaction(noteId) },
                            onReplyClick = { onReply(noteId) },
                            getOpenGraphMetadata = getOpenGraphMetadata,
                            onNoteClick = onNavigateToThread,
                            onProfileClick = onProfileClick,
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(Modifier.height(32.dp))
                    }

                    is LeafNote -> ThreadNote(
                        uiModel = note.noteUiModel,
                        modifier = Modifier.clickable { onNavigateToThread(noteId) },
                        onAvatarClick = { onProfileClick(note.pubkey) },
                        onLikeClick = { onNoteReaction(noteId) },
                        onReplyClick = { onReply(noteId) },
                        showConnector = note.showConnector,
                        getOpenGraphMetadata = getOpenGraphMetadata,
                        onProfileClick = onProfileClick,
                        onNoteClick = onNavigateToThread
                    )
                }

                LaunchedEffect(Unit) {
                    onNoteDisplayed(noteId, note.pubkey)
                }

                DisposableEffect(Unit) {
                    onDispose { onNoteDisposed(noteId, note.pubkey) }
                }
            }
        }
    }

}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewThread() {
    PlasmaTheme {
        ThreadList(
            onNavigateBack = {},
            onNavigateToThread = {},
            onNavigateToProfile = {},
            onNavigateToReply = {}
        )
    }
}

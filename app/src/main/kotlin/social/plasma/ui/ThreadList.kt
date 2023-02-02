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
import social.plasma.PubKey
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
    onNavigateToThread: (String) -> Unit,
    onNavigateToProfile: (PubKey) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    ThreadList(
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        uiState = uiState,
        onNavigateToThread = onNavigateToThread,
        onNoteDisplayed = viewModel::onNoteDisplayed,
        onNoteDisposed = viewModel::onNoteDisposed,
        onAvatarClick = onNavigateToProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThreadList(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    uiState: ThreadUiState,
    onNavigateToThread: (String) -> Unit,
    onNoteDisplayed: (String, PubKey) -> Unit,
    onNoteDisposed: (String, PubKey) -> Unit,
    onAvatarClick: (PubKey) -> Unit,
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
            modifier = Modifier.fillMaxSize().padding(it),
            state = state,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(uiState.noteUiModels) { threadUiModel ->
                when (threadUiModel) {
                    is RootNote -> Column {
                        NoteFlatCard(
                            uiModel = threadUiModel.noteUiModel,
                            onAvatarClick = { onAvatarClick(threadUiModel.pubkey) },
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(Modifier.height(32.dp))
                    }

                    is LeafNote -> ThreadNote(
                        uiModel = threadUiModel.noteUiModel,
                        onAvatarClick = { onAvatarClick(threadUiModel.pubkey) },
                        modifier = Modifier.clickable { onNavigateToThread(threadUiModel.id) })
                }

                LaunchedEffect(Unit) {
                    onNoteDisplayed(threadUiModel.id, threadUiModel.pubkey)
                }

                DisposableEffect(Unit) {
                    onDispose { onNoteDisposed(threadUiModel.id, threadUiModel.pubkey) }
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
            onNavigateToProfile = {}
        )
    }
}

package social.plasma.ui.feed

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import kotlinx.coroutines.flow.Flow
import social.plasma.models.PubKey
import social.plasma.ui.components.NoteCard
import social.plasma.ui.components.NoteCardUiModel
import social.plasma.ui.components.ProgressIndicator

@Composable
fun Feed(
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
    onNavigateToProfile: (PubKey) -> Unit,
) {
    val uiState by viewModel.uiState().collectAsState()

    FeedContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateToProfile = onNavigateToProfile,
    )
}

@Composable
private fun FeedContent(
    modifier: Modifier = Modifier,
    uiState: FeedUiState,
    onNavigateToProfile: (PubKey) -> Unit,
) {
    when (uiState) {
        is FeedUiState.Loading -> ProgressIndicator(modifier = modifier)
        is FeedUiState.Loaded -> FeedList(
            modifier = modifier,
            noteList = uiState.feedPagingFlow,
            onNavigateToProfile = onNavigateToProfile
        )
    }
}

@Composable
private fun FeedList(
    noteList: Flow<PagingData<NoteCardUiModel>>,
    modifier: Modifier = Modifier,
    onNavigateToProfile: (PubKey) -> Unit,
) {
    val pagingLazyItems = noteList.collectAsLazyPagingItems()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(pagingLazyItems) { note ->
            note?.let {
                NoteCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    uiModel = it,
                    onAvatarClick = { onNavigateToProfile(note.userPubkey) },
                )
            }
        }
    }
}

package social.plasma.ui.feed

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import social.plasma.models.PubKey
import social.plasma.ui.components.NoteCard
import social.plasma.ui.components.NoteCardUiModel
import social.plasma.ui.components.ProgressIndicator
import social.plasma.ui.theme.PlasmaTheme

@Composable
fun Feed(
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
    onNavigateToProfile: (PubKey) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

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
            noteList = uiState.cardList,
            onNavigateToProfile = onNavigateToProfile
        )
    }
}

@Composable
private fun FeedList(
    noteList: List<NoteCardUiModel>,
    modifier: Modifier = Modifier,
    onNavigateToProfile: (PubKey) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // TODO add keys from Note ID
        items(noteList) { note ->
            NoteCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                uiModel = note,
                onAvatarClick = { onNavigateToProfile(note.userPubkey) },
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PreviewFeedList() {
    val uiState =
        FeedUiState.Loaded(cardList = (0..50).map {
            NoteCardUiModel(
                id = "id",
                name = "$it",
                nip5 = "nostrplebs.com",
                content = "Content $it",
                timePosted = "1m",
                avatarUrl = "https://api.dicebear.com/5.x/bottts/jpg?seed=$it",
                likeCount = "490k",
                replyCount = "25k",
                shareCount = "1.5k",
                userPubkey = PubKey("fsdf")
            )
        })

    PlasmaTheme {
        FeedContent(uiState = uiState, onNavigateToProfile = { })
    }
}

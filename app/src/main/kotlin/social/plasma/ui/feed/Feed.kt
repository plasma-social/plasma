package social.plasma.ui.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import social.plasma.models.Note
import social.plasma.ui.theme.PlasmaTheme

@Composable
fun Feed(
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    FeedContent(modifier = modifier, uiState = uiState)
}

@Composable
private fun FeedContent(
    modifier: Modifier = Modifier,
    uiState: FeedListUiState,
) {
    when (uiState) {
        is FeedListUiState.Loading -> Loading(modifier = modifier)
        is FeedListUiState.Loaded -> FeedList(modifier = modifier, noteList = uiState.noteList)
    }
}

@Composable
private fun Loading(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Loading...")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedList(
    noteList: List<Note>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        // TODO add keys from Note ID
        items(noteList) { note ->
            ListItem(headlineText = {
                Text(text = note.content)
            })
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewFeedList() {
    val uiState = FeedListUiState.Loaded(noteList = (0..50).map { Note("Note $it") })

    PlasmaTheme {
        FeedContent(uiState = uiState)
    }
}

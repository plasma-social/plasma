package social.plasma.ui.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import social.plasma.ui.components.FeedCard
import social.plasma.ui.components.FeedCardUiModel
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
    uiState: FeedUiState,
) {
    when (uiState) {
        is FeedUiState.Loading -> Loading(modifier = modifier)
        is FeedUiState.Loaded -> FeedList(modifier = modifier, noteList = uiState.cardList)
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

@Composable
private fun FeedList(
    noteList: List<FeedCardUiModel>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        // TODO add keys from Note ID
        items(noteList) { note ->
            FeedCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                uiModel = note
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewFeedList() {
    val uiState =
        FeedUiState.Loaded(cardList = (0..50).map {
            FeedCardUiModel(
                id = "id",
                name = "$it",
                nip5 = "notrplebs.com",
                content = "Content $it",
                timePosted = "1m",
                imageUrl = "https://api.dicebear.com/5.x/bottts/jpg?seed=$it"
            )
        })

    PlasmaTheme {
        FeedContent(uiState = uiState)
    }
}

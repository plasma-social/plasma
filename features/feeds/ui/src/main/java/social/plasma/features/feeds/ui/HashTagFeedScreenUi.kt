package social.plasma.features.feeds.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.slack.circuit.Ui
import social.plasma.features.feeds.screens.hashtags.HashTagScreenUiEvent
import social.plasma.features.feeds.screens.hashtags.HashTagScreenUiState
import social.plasma.ui.R

@OptIn(ExperimentalMaterial3Api::class)
class HashTagFeedScreenUi : Ui<HashTagScreenUiState> {
    @Composable
    override fun Content(state: HashTagScreenUiState, modifier: Modifier) {
        val onScreenEvent = state.onEvent

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(state.title) },
                    navigationIcon = {
                        IconButton(onClick = { onScreenEvent(HashTagScreenUiEvent.OnNavigateBack) }) {
                            Icon(
                                painterResource(R.drawable.ic_chevron_back),
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
        ) { paddingValues ->
            FeedUiContent(
                state = state.feedState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 8.dp),
            )
        }
    }
}

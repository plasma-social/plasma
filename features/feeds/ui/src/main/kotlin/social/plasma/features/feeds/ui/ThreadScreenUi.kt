package social.plasma.features.feeds.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.slack.circuit.runtime.ui.Ui
import social.plasma.features.feeds.screens.threads.ThreadScreenUiEvent
import social.plasma.features.feeds.screens.threads.ThreadScreenUiState
import social.plasma.ui.R

@OptIn(ExperimentalMaterial3Api::class)
class ThreadScreenUi : Ui<ThreadScreenUiState> {
    @Composable
    override fun Content(state: ThreadScreenUiState, modifier: Modifier) {
        val onScreenEvent = state.onEvent

        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(state.title) },
                    navigationIcon = {
                        IconButton(onClick = { onScreenEvent(ThreadScreenUiEvent.OnBackClick) }) {
                            Icon(
                                painterResource(R.drawable.ic_chevron_back),
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
        ) { paddingValue ->
            EventFeedUi(
                modifier = Modifier.padding(paddingValue),
                state = state.eventFeedUiState,
            )
        }
    }
}

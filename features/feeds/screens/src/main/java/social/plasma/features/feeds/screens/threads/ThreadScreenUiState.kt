package social.plasma.features.feeds.screens.threads

import com.slack.circuit.runtime.CircuitUiState
import social.plasma.features.feeds.screens.eventfeed.EventFeedUiState

data class ThreadScreenUiState(
    val title: String,
    val eventFeedUiState: EventFeedUiState,
    val onEvent: (ThreadScreenUiEvent) -> Unit,
) : CircuitUiState

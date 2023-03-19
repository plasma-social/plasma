package social.plasma.features.feeds.screens.homefeeds

import com.slack.circuit.CircuitUiState

data class HomeFeedsUiState(
    val onEvent: (HomeFeedsUiEvent) -> Unit,
) : CircuitUiState
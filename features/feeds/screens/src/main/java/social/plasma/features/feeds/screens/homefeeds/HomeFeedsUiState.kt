package social.plasma.features.feeds.screens.homefeeds

import com.slack.circuit.runtime.CircuitUiState

data class HomeFeedsUiState(
    val title: String,
    val toolbarAvatar: String?,
    val relayConnectionRatio: String?,
    val onEvent: (HomeFeedsUiEvent) -> Unit,
) : CircuitUiState

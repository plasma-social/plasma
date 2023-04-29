package social.plasma.features.feeds.screens.homefeeds

import com.slack.circuit.CircuitUiState

data class HomeFeedsUiState(
    val title: String,
    val toolbarAvatar: String?,
    val onEvent: (HomeFeedsUiEvent) -> Unit,
) : CircuitUiState

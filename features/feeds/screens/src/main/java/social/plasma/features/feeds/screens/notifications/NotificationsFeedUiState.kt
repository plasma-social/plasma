package social.plasma.features.feeds.screens.notifications

import com.slack.circuit.CircuitUiState

data class NotificationsFeedUiState(
    val title: String,
    val toolbarAvatar: String?,
    val onEvent: (NotificationsFeedUiEvent) -> Unit,
) : CircuitUiState

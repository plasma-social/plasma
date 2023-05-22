package social.plasma.features.feeds.screens.notifications

import com.slack.circuit.runtime.CircuitUiState

data class NotificationsFeedUiState(
    val title: String,
    val toolbarAvatar: String?,
    val onEvent: (NotificationsFeedUiEvent) -> Unit,
) : CircuitUiState

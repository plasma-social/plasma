package social.plasma.features.discovery.screens.communities

import com.slack.circuit.runtime.CircuitUiState

data class CommunityListItemUiState(
    val name: String,
    val onEvent: (CommunityListItemEvent) -> Unit,
) : CircuitUiState

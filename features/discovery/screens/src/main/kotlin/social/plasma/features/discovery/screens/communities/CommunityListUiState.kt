package social.plasma.features.discovery.screens.communities

import com.slack.circuit.runtime.CircuitUiState

data class CommunityListUiState(
    val followedHashTags: List<String>,
    val onEvent: (CommunityListUiEvent) -> Unit,
) : CircuitUiState

package social.plasma.features.discovery.screens.communities

import com.slack.circuit.runtime.CircuitUiState
import social.plasma.models.HashTag

data class CommunityListUiState(
    val followedHashTags: List<HashTag>,
    val recommendedHashTags: List<HashTag>,
    val onEvent: (CommunityListUiEvent) -> Unit,
) : CircuitUiState

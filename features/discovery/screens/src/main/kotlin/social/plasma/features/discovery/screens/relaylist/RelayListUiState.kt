package social.plasma.features.discovery.screens.relaylist

import com.slack.circuit.CircuitUiState

data class RelayListUiState(
    val title: String,
    val relayUiState: List<RelayUiState> = emptyList(),
    val onEvent: (RelayListUiEvent) -> Unit,
) : CircuitUiState {
}

data class RelayUiState(
    val name: String,
    val status: RelayStatus,
)

enum class RelayStatus {
    CONNECTED,
    CONNECTING,
    DISCONNECTED
}

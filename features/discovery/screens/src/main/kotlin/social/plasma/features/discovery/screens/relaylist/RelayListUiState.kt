package social.plasma.features.discovery.screens.relaylist

import com.slack.circuit.CircuitUiState

data class RelayListUiState(
    val onEvent: (RelayListUiEvent) -> Unit,
) : CircuitUiState

package social.plasma.features.search.screens

import com.slack.circuit.CircuitUiState

data class SearchUiState(
    val onEvent: (SearchUiEvent) -> Unit,
) : CircuitUiState

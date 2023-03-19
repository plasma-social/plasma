package social.plasma.features.posting.screens

import com.slack.circuit.CircuitUiState

data class ComposePostUiState(
    val postButtonEnabled: Boolean = false,
    val title: String = "",
    val placeholder: String = "",
    val postButtonLabel: String = "",
    val onEvent: (ComposePostUiEvent) -> Unit,
) : CircuitUiState

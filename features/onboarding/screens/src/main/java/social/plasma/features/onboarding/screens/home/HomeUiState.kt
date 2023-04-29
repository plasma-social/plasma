package social.plasma.features.onboarding.screens.home

import com.slack.circuit.CircuitUiState

data class HomeUiState(
    val onEvent: (HomeUiEvent) -> Unit,
) : CircuitUiState

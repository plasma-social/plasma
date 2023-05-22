package social.plasma.features.onboarding.screens.home

import com.slack.circuit.runtime.CircuitUiState

data class HomeUiState(
    val onEvent: (HomeUiEvent) -> Unit,
) : CircuitUiState

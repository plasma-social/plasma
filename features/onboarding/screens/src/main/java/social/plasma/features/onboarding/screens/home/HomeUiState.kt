package social.plasma.features.onboarding.screens.home

import com.slack.circuit.CircuitUiState

data class HomeUiState(
    val avatarUrl: String?,
    val onEvent: (HomeUiEvent) -> Unit,
) : CircuitUiState
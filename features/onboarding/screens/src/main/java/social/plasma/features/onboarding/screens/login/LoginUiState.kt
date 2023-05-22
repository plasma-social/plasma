package social.plasma.features.onboarding.screens.login

import com.slack.circuit.runtime.CircuitUiState

data class LoginUiState(
    val keyInput: String,
    val loginButtonVisible: Boolean,
    val clearInputButtonVisible: Boolean,
    val onEvent: (LoginUiEvent) -> Unit,
) : CircuitUiState

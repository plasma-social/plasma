package social.plasma.features.onboarding.screens.login

import com.slack.circuit.runtime.CircuitUiEvent

sealed interface LoginUiEvent : CircuitUiEvent {
    object OnLogin : LoginUiEvent
    object OnClearInput : LoginUiEvent

    data class OnInputChange(val value: String) : LoginUiEvent
}

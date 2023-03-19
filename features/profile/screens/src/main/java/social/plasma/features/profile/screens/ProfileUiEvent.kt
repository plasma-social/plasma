package social.plasma.features.profile.screens

import com.slack.circuit.CircuitUiEvent

sealed interface ProfileUiEvent : CircuitUiEvent {
    object OnNavigateBack : ProfileUiEvent
}

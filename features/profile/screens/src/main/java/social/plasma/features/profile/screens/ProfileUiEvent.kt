package social.plasma.features.profile.screens

import com.slack.circuit.runtime.CircuitUiEvent

sealed interface ProfileUiEvent : CircuitUiEvent {
    object OnNavigateBack : ProfileUiEvent

    object OnFollowButtonClicked : ProfileUiEvent
}

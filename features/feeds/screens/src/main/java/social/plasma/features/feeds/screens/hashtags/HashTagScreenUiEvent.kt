package social.plasma.features.feeds.screens.hashtags

import com.slack.circuit.CircuitUiEvent

sealed interface HashTagScreenUiEvent : CircuitUiEvent {
    object OnNavigateBack : HashTagScreenUiEvent
}

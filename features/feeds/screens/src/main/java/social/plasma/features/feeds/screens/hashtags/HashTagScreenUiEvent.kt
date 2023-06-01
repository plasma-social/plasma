package social.plasma.features.feeds.screens.hashtags

import com.slack.circuit.runtime.CircuitUiEvent

sealed interface HashTagScreenUiEvent : CircuitUiEvent {
    object OnNavigateBack : HashTagScreenUiEvent
    object OnFollowButtonClick : HashTagScreenUiEvent
}

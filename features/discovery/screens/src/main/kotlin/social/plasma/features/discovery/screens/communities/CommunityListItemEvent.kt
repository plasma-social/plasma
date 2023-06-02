package social.plasma.features.discovery.screens.communities

import com.slack.circuit.runtime.CircuitUiEvent

sealed interface CommunityListItemEvent : CircuitUiEvent {
    object OnClick : CommunityListItemEvent
}

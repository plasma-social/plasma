package social.plasma.features.discovery.screens.communities

import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.runtime.CircuitUiState

sealed interface CommunityListUiEvent : CircuitUiState {
    data class OnChildNavEvent(val navEvent: NavEvent) : CommunityListUiEvent
}

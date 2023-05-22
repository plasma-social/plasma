package social.plasma.features.discovery.screens.relaylist

import com.slack.circuit.runtime.CircuitUiEvent

sealed interface RelayListUiEvent : CircuitUiEvent {
    object OnBackPressed : RelayListUiEvent
}

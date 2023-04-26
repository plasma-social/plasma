package social.plasma.features.search.screens

import com.slack.circuit.CircuitUiEvent

sealed interface SearchUiEvent : CircuitUiEvent {
    object OnSearch : SearchUiEvent
    data class OnQueryChanged(val query: String) : SearchUiEvent
    data class OnActiveChanged(val active: Boolean) : SearchUiEvent

    object OnLeadingIconTapped : SearchUiEvent

    object OnTrailingIconTapped : SearchUiEvent
}

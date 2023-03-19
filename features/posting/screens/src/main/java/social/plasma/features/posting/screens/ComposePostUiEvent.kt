package social.plasma.features.posting.screens

import com.slack.circuit.CircuitUiEvent


sealed interface ComposePostUiEvent : CircuitUiEvent {
    data class OnNoteChange(val content: String) : ComposePostUiEvent
    object OnSubmitPost : ComposePostUiEvent
    object OnBackClick : ComposePostUiEvent
}

package social.plasma.features.feeds.screens.notes

import com.slack.circuit.runtime.CircuitUiEvent

sealed interface QuotedNoteEvent : CircuitUiEvent {
    object OnAvatarClicked : QuotedNoteEvent
    object OnNoteClicked : QuotedNoteEvent
}

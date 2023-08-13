package social.plasma.features.feeds.screens.feeditems.quotednotes

import com.slack.circuit.runtime.CircuitUiEvent

sealed interface QuotedNoteEvent : CircuitUiEvent {
    object OnAvatarClicked : QuotedNoteEvent
    object OnNoteClicked : QuotedNoteEvent
}

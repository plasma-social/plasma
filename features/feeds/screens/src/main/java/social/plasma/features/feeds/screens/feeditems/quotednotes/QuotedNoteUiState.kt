package social.plasma.features.feeds.screens.feeditems.quotednotes

import com.slack.circuit.runtime.CircuitUiState
import social.plasma.features.feeds.screens.feed.FeedItem

sealed interface QuotedNoteUiState : CircuitUiState {
    object Loading : QuotedNoteUiState
    object NoteNotFound : QuotedNoteUiState

    data class Loaded(
        val note: FeedItem.NoteCard,
        val onEvent: (QuotedNoteEvent) -> Unit,
    ) : QuotedNoteUiState
}



package social.plasma.features.feeds.screens.notes

import com.slack.circuit.runtime.CircuitUiState
import social.plasma.features.feeds.screens.feed.FeedItem


data class QuotedNoteUiState(
    val note: FeedItem.NoteCard?,
    val onEvent: (QuotedNoteEvent) -> Unit,
) : CircuitUiState

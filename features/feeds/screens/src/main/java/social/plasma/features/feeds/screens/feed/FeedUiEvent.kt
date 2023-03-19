package social.plasma.features.feeds.screens.feed

import com.slack.circuit.CircuitUiEvent
import social.plasma.models.NoteId
import social.plasma.models.PubKey

sealed interface FeedUiEvent : CircuitUiEvent {
    data class OnNoteClick(val noteId: NoteId) : FeedUiEvent

    data class OnNoteRepost(val noteId: NoteId) : FeedUiEvent

    data class OnProfileClick(val pubKey: PubKey) : FeedUiEvent

    data class OnNoteReaction(val noteId: NoteId) : FeedUiEvent

    data class OnReplyClick(val noteId: NoteId) : FeedUiEvent

    data class OnNoteDisplayed(val noteId: NoteId, val pubKey: PubKey) : FeedUiEvent
}
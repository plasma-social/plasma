package social.plasma.ui.feed

import social.plasma.models.NoteId
import social.plasma.models.PubKey

sealed interface FeedUiEvent {
    data class OnNoteDisposed(
        val noteId: NoteId,
        val pubKey: PubKey,
    ) : FeedUiEvent

    data class OnNoteDisplayed(
        val noteId: NoteId,
        val pubKey: PubKey,
    ) : FeedUiEvent

    data class OnNoteReaction(
        val noteId: NoteId,
    ) : FeedUiEvent
}

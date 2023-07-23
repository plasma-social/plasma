package social.plasma.features.feeds.screens.feed

import app.cash.nostrino.crypto.PubKey
import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.runtime.CircuitUiEvent
import social.plasma.models.NoteId
import social.plasma.models.TipAddress

sealed interface FeedUiEvent : CircuitUiEvent {
    data class OnNoteClick(val noteId: NoteId) : FeedUiEvent

    data class OnNoteRepost(val noteId: NoteId) : FeedUiEvent

    data class OnProfileClick(val pubKey: PubKey) : FeedUiEvent

    data class OnNoteReaction(val noteId: NoteId) : FeedUiEvent

    data class OnReplyClick(val noteId: NoteId) : FeedUiEvent

    data class OnZapClick(
        val tipAddress: TipAddress?,
        val satAmount: Long,
        val pubKey: PubKey,
        val noteId: NoteId,
    ) : FeedUiEvent

    data class OnNoteDisplayed(val noteId: NoteId, val pubKey: PubKey) : FeedUiEvent

    data class OnFeedCountChange(val count: Int) : FeedUiEvent

    object OnRefreshButtonClick : FeedUiEvent

    data class OnHashTagClick(val hashTag: String) : FeedUiEvent
    data class OnNavEvent(val navEvent: NavEvent) : FeedUiEvent
}

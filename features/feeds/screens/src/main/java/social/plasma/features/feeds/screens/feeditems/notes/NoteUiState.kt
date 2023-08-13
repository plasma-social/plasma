package social.plasma.features.feeds.screens.feeditems.notes

import app.cash.nostrino.crypto.PubKey
import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.models.NoteId

sealed interface NoteUiState : CircuitUiState {
    object Loading : NoteUiState

    data class Loaded(
        val noteCard: FeedItem.NoteCard,
        val onEvent: (NoteUiEvent) -> Unit,
    ) : NoteUiState
}


sealed interface NoteUiEvent : CircuitUiEvent {
    data class OnProfileClick(val pubKey: PubKey) : NoteUiEvent
    data class OnNoteClick(val noteId: NoteId) : NoteUiEvent
    data class OnHashTagClick(val hashTag: String) : NoteUiEvent
    object OnAvatarClick : NoteUiEvent
    object OnLikeClick : NoteUiEvent
    object OnReplyClick : NoteUiEvent
    object OnRepostClick : NoteUiEvent
    data class OnZapClick(val satAmount: Long) : NoteUiEvent
    data class OnNestedNavEvent(val navEvent: NavEvent) : NoteUiEvent

    object OnClick : NoteUiEvent
}

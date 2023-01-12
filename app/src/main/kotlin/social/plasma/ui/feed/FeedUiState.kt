package social.plasma.ui.feed

import social.plasma.ui.components.NoteCardUiModel

sealed interface FeedUiState {
    object Loading : FeedUiState
    data class Loaded(val cardList: List<NoteCardUiModel>) : FeedUiState
}

package social.plasma.ui.feed

import social.plasma.models.Note

sealed interface FeedListUiState {
    object Loading : FeedListUiState
    data class Loaded(val noteList: List<Note>) : FeedListUiState
}

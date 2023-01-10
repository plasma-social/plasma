package social.plasma.ui.feed

import social.plasma.ui.components.FeedCardUiModel

sealed interface FeedUiState {
    object Loading : FeedUiState
    data class Loaded(val cardList: List<FeedCardUiModel>) : FeedUiState
}

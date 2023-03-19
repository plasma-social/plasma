package social.plasma.features.feeds.screens.threads

import social.plasma.features.feeds.screens.feed.FeedUiEvent

sealed interface ThreadScreenUiEvent {
    data class OnFeedEvent(val feedUiEvent: FeedUiEvent) : ThreadScreenUiEvent

    object OnBackClick : ThreadScreenUiEvent
}

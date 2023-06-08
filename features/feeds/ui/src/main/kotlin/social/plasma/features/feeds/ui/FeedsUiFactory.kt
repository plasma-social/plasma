package social.plasma.features.feeds.ui

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.ui.Ui
import social.plasma.features.feeds.screens.feed.FeedScreen
import social.plasma.features.feeds.screens.homefeeds.HomeFeeds
import social.plasma.features.feeds.screens.notes.QuotedNoteScreen
import social.plasma.features.feeds.screens.notifications.NotificationsFeedScreen
import social.plasma.features.feeds.screens.threads.HashTagFeedScreen
import social.plasma.features.feeds.screens.threads.ThreadScreen
import social.plasma.features.feeds.ui.notes.QuotedNoteUi
import javax.inject.Inject

class FeedsUiFactory @Inject constructor() : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when (screen) {
            is HomeFeeds -> HomeFeedsUi()
            is FeedScreen -> FeedUi()
            is HashTagFeedScreen -> HashTagFeedScreenUi()
            is ThreadScreen -> ThreadScreenUi()
            is NotificationsFeedScreen -> NotificationsFeedScreenUi()
            is QuotedNoteScreen -> QuotedNoteUi()
            else -> null
        }
    }
}

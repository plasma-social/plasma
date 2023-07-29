package social.plasma.features.feeds.ui

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import social.plasma.features.feeds.screens.eventfeed.EventFeedUiState
import social.plasma.features.feeds.screens.feed.FeedScreen
import social.plasma.features.feeds.screens.feeditems.notes.NoteScreen
import social.plasma.features.feeds.screens.feeditems.notes.NoteUiState
import social.plasma.features.feeds.screens.feeditems.quotednotes.QuotedNoteScreen
import social.plasma.features.feeds.screens.homefeeds.HomeFeeds
import social.plasma.features.feeds.screens.notifications.NotificationsFeedScreen
import social.plasma.features.feeds.screens.threads.HashTagFeedScreen
import social.plasma.features.feeds.screens.threads.ThreadScreen
import social.plasma.features.feeds.ui.notes.NoteScreenUi
import social.plasma.features.feeds.ui.notes.QuotedNoteUi
import javax.inject.Inject

class FeedsUiFactory @Inject constructor() : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when (screen) {
            is HomeFeeds -> HomeFeedsUi()
            is FeedScreen -> eventFeedUi()
            is HashTagFeedScreen -> HashTagFeedScreenUi()
            is ThreadScreen -> ThreadScreenUi()
            is NotificationsFeedScreen -> NotificationsFeedScreenUi()
            is QuotedNoteScreen -> QuotedNoteUi()
            is NoteScreen -> ui<NoteUiState> { state, modifier -> NoteScreenUi(state, modifier) }
            else -> null
        }
    }

    private fun eventFeedUi() = ui<EventFeedUiState> { uiState, modifier ->
        EventFeedUi(
            modifier = modifier,
            state = uiState
        )
    }
}

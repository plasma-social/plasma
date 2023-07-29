package social.plasma.features.feeds.screens.eventfeed

import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.runtime.CircuitUiEvent

sealed interface EventFeedUiEvent : CircuitUiEvent {
    data class OnFeedCountChange(val itemCount: Int) : EventFeedUiEvent
    data class OnChildNavEvent(val navEvent: NavEvent) : EventFeedUiEvent
    object OnRefreshButtonClick : EventFeedUiEvent
}

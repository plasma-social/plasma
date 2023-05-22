package social.plasma.features.feeds.screens.notifications

import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.runtime.CircuitUiEvent

sealed interface NotificationsFeedUiEvent : CircuitUiEvent {
    object OnToolbarAvatarTapped : NotificationsFeedUiEvent

    data class ChildNav(val navEvent: NavEvent) : NotificationsFeedUiEvent
}

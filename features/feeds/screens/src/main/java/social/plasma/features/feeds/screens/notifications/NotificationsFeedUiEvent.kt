package social.plasma.features.feeds.screens.notifications

import com.slack.circuit.CircuitUiEvent
import com.slack.circuit.NavEvent

sealed interface NotificationsFeedUiEvent : CircuitUiEvent {
    object OnToolbarAvatarTapped : NotificationsFeedUiEvent

    data class ChildNav(val navEvent: NavEvent) : NotificationsFeedUiEvent
}

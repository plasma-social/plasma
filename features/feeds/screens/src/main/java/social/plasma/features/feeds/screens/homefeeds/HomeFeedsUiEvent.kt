package social.plasma.features.feeds.screens.homefeeds

import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.runtime.CircuitUiEvent

interface HomeFeedsUiEvent : CircuitUiEvent {
    object OnToolbarAvatarTapped : HomeFeedsUiEvent

    object OnRelayInfoTapped : HomeFeedsUiEvent

    data class ChildNav(val navEvent: NavEvent) : HomeFeedsUiEvent
}

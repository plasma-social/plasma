package social.plasma.features.feeds.screens.homefeeds

import com.slack.circuit.CircuitUiEvent
import com.slack.circuit.NavEvent

interface HomeFeedsUiEvent : CircuitUiEvent {
    object OnToolbarAvatarTapped : HomeFeedsUiEvent

    object OnRelayInfoTapped : HomeFeedsUiEvent
    
    data class ChildNav(val navEvent: NavEvent) : HomeFeedsUiEvent
}

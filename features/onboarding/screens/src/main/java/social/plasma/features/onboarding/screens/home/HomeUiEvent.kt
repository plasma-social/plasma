package social.plasma.features.onboarding.screens.home

import com.slack.circuit.CircuitUiEvent
import com.slack.circuit.NavEvent

sealed interface HomeUiEvent : CircuitUiEvent {
    object OnFabClick : HomeUiEvent
    object OnAvatarClick : HomeUiEvent

    data class OnChildNav(val navEvent: NavEvent) : HomeUiEvent
}
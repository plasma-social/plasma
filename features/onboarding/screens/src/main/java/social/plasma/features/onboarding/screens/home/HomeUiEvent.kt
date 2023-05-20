package social.plasma.features.onboarding.screens.home

import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.runtime.CircuitUiEvent

sealed interface HomeUiEvent : CircuitUiEvent {
    object OnFabClick : HomeUiEvent

    data class OnChildNav(val navEvent: NavEvent) : HomeUiEvent
}

package social.plasma.onboarding.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import social.plasma.domain.observers.ObserveHasPendingNotifications
import social.plasma.features.onboarding.screens.home.HomeUiEvent
import social.plasma.features.onboarding.screens.home.HomeUiEvent.OnFabClick
import social.plasma.features.onboarding.screens.home.HomeUiState
import social.plasma.features.posting.screens.ComposingScreen

class HomePresenter @AssistedInject constructor(
    private val observeHasPendingNotifications: ObserveHasPendingNotifications,
    @Assisted private val navigator: Navigator,
) : Presenter<HomeUiState> {

    @Composable
    override fun present(): HomeUiState {
        var navigationInFlight by rememberRetained { mutableStateOf(false) }
        val hasNewNotifications by remember {
            observeHasPendingNotifications.flow.onStart {
                observeHasPendingNotifications(Unit)
            }
        }.collectAsState(initial = false)

        val coroutineScope = rememberCoroutineScope()
        return HomeUiState(
            showNotificationsBadge = hasNewNotifications,
        ) { event ->
            when (event) {
                OnFabClick -> coroutineScope.launch {
                    if (!navigationInFlight) {
                        navigationInFlight = true
                        navigator.goTo(ComposingScreen())
                        delay(1000) // debounce
                        navigationInFlight = false
                    }
                }

                is HomeUiEvent.OnChildNav -> navigator.onNavEvent(event.navEvent)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): HomePresenter
    }
}

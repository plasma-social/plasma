package social.plasma.onboarding.presenters

import android.os.SystemClock
import androidx.compose.runtime.Composable
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import com.slack.circuit.onNavEvent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import social.plasma.features.onboarding.screens.home.HomeUiEvent
import social.plasma.features.onboarding.screens.home.HomeUiEvent.OnFabClick
import social.plasma.features.onboarding.screens.home.HomeUiState
import social.plasma.features.posting.screens.ComposingScreen

class HomePresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
) : Presenter<HomeUiState> {

    @Composable
    override fun present(): HomeUiState {
        var mLastClickTime: Long = 0
        return HomeUiState { event ->
            when (event) {
                OnFabClick -> {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return@HomeUiState
                    } else {
                        mLastClickTime = SystemClock.elapsedRealtime()
                        navigator.goTo(ComposingScreen())
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

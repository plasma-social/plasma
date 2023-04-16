package social.plasma.feeds.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import com.slack.circuit.onNavEvent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collect
import social.plasma.domain.interactors.SyncContactsEvents
import social.plasma.features.feeds.screens.homefeeds.HomeFeedsUiEvent
import social.plasma.features.feeds.screens.homefeeds.HomeFeedsUiState

class HomeFeedsPresenter @AssistedInject constructor(
    private val syncContactsEvents: SyncContactsEvents,
    @Assisted private val navigator: Navigator,
) : Presenter<HomeFeedsUiState> {

    @Composable
    override fun present(): HomeFeedsUiState {
        LaunchedEffect(Unit) {
            syncContactsEvents(Unit)
            syncContactsEvents.flow.collect()
        }

        return HomeFeedsUiState { event ->
            when(event) {
               is HomeFeedsUiEvent.ChildNav -> {
                   navigator.onNavEvent(event.navEvent)
               }
            }
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): HomeFeedsPresenter
    }
}

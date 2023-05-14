package social.plasma.features.discovery.presenters

import androidx.compose.runtime.Composable
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import social.plasma.features.discovery.screens.relaylist.RelayListUiState

class RelayListPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
) : Presenter<RelayListUiState> {
    @Composable
    override fun present(): RelayListUiState {
        return RelayListUiState {

        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): RelayListPresenter
    }
}

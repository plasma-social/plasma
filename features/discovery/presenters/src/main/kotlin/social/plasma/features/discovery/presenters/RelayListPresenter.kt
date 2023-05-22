package social.plasma.features.discovery.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.combine
import social.plasma.features.discovery.screens.relaylist.RelayListUiEvent
import social.plasma.features.discovery.screens.relaylist.RelayListUiState
import social.plasma.features.discovery.screens.relaylist.RelayStatus
import social.plasma.features.discovery.screens.relaylist.RelayUiState
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.RelayManager

class RelayListPresenter @AssistedInject constructor(
    private val relayManager: RelayManager,
    @Assisted private val navigator: Navigator,
) : Presenter<RelayListUiState> {


    @Composable
    override fun present(): RelayListUiState {
        val relays by remember { relayManager.relayList }.collectAsState()

        val relayUiState by produceState<List<RelayUiState>>(initialValue = emptyList(), relays) {
            combine(relays.map { it.connectionStatus }) { statuses ->
                statuses.mapIndexed { index, status ->
                    RelayUiState(
                        name = relays[index].url,
                        status = when (status.status) {
                            Relay.Status.Initial -> RelayStatus.CONNECTING
                            Relay.Status.Connected -> RelayStatus.CONNECTED
                            is Relay.Status.ConnectionClosed,
                            is Relay.Status.ConnectionClosing,
                            is Relay.Status.ConnectionFailed,
                            -> RelayStatus.DISCONNECTED
                        }
                    )
                }
            }.collect {
                value = it
            }
        }

        return RelayListUiState(
            title = "Relays",
            relayUiState = relayUiState,
        ) { event ->
            when (event) {
                RelayListUiEvent.OnBackPressed -> navigator.pop()
            }
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): RelayListPresenter
    }
}

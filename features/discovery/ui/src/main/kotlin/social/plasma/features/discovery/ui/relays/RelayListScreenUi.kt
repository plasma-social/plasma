package social.plasma.features.discovery.ui.relays

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.Ui
import social.plasma.features.discovery.screens.relaylist.RelayListUiState

class RelayListScreenUi : Ui<RelayListUiState> {
    @Composable
    override fun Content(state: RelayListUiState, modifier: Modifier) {
        LazyColumn {
            item {
                Text("Relay List")
            }
        }
    }
}

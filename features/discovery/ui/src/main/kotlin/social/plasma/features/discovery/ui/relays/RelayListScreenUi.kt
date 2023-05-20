package social.plasma.features.discovery.ui.relays

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Badge
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.ui.Ui
import social.plasma.features.discovery.screens.relaylist.RelayListUiEvent
import social.plasma.features.discovery.screens.relaylist.RelayListUiState
import social.plasma.features.discovery.screens.relaylist.RelayStatus

class RelayListScreenUi : Ui<RelayListUiState> {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(state: RelayListUiState, modifier: Modifier) {
        val onEvent = state.onEvent

        Scaffold(topBar = {
            CenterAlignedTopAppBar(title = { Text(state.title) }, navigationIcon = {
                IconButton(onClick = { onEvent(RelayListUiEvent.OnBackPressed) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null)
                }
            })
        }) { paddingValues ->
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                items(state.relayUiState) { item ->
                    ListItem(modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(
                            RoundedCornerShape(8.dp)
                        ), leadingContent = {
                        when (item.status) {
                            RelayStatus.CONNECTED -> Badge(containerColor = Color.Green)
                            RelayStatus.CONNECTING -> Badge(containerColor = Color(0xFFFFA500))
                            RelayStatus.DISCONNECTED -> Badge(containerColor = Color.Red)
                        }
                    }, headlineContent = { Text(item.name) }, supportingContent = {
                        Text(item.status.name)
                    })
                }
            }
        }

    }
}

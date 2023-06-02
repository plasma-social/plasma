package social.plasma.features.discovery.ui.community

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.runtime.ui.Ui
import social.plasma.features.discovery.screens.communities.CommunityListItemScreen
import social.plasma.features.discovery.screens.communities.CommunityListUiEvent.OnChildNavEvent
import social.plasma.features.discovery.screens.communities.CommunityListUiState

class CommunityListUi : Ui<CommunityListUiState> {
    @Composable
    override fun Content(state: CommunityListUiState, modifier: Modifier) {
        val onEvent = state.onEvent

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(state.followedHashTags) { hashtag ->
                CircuitContent(
                    screen = CommunityListItemScreen(hashtag),
                    onNavEvent = { onEvent(OnChildNavEvent(it)) },
                )
            }
        }
    }
}

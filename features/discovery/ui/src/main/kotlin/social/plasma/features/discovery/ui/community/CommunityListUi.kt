package social.plasma.features.discovery.ui.community

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.runtime.ui.Ui
import social.plasma.features.discovery.screens.communities.CommunityListItemScreen
import social.plasma.features.discovery.screens.communities.CommunityListUiEvent.OnChildNavEvent
import social.plasma.features.discovery.screens.communities.CommunityListUiState
import social.plasma.features.discovery.ui.R

class CommunityListUi : Ui<CommunityListUiState> {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content(state: CommunityListUiState, modifier: Modifier) {
        val onEvent = state.onEvent

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {

            if (state.followedHashTags.isNotEmpty()) {
                stickyHeader(key = "following") {
                    Text(
                        text = stringResource(R.string.following),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(vertical = 4.dp),
                    )
                }
            }
            items(state.followedHashTags, key = { it }) { hashtag ->
                CircuitContent(
                    screen = CommunityListItemScreen(hashtag),
                    onNavEvent = { onEvent(OnChildNavEvent(it)) },
                )
            }

            if (state.recommendedHashTags.isNotEmpty()) {
                stickyHeader(key = "recommended") {
                    Text(
                        text = stringResource(R.string.recommended),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background),
                    )
                }
            }

            items(state.recommendedHashTags, key = { it }) { hashtag ->
                CircuitContent(
                    screen = CommunityListItemScreen(hashtag),
                    onNavEvent = { onEvent(OnChildNavEvent(it)) },
                )
            }
        }
    }
}

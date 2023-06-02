package social.plasma.features.discovery.ui.community

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.ui.Ui
import social.plasma.features.discovery.screens.communities.CommunityListItemEvent
import social.plasma.features.discovery.screens.communities.CommunityListItemUiState
import social.plasma.ui.components.Avatar
import social.plasma.ui.theme.PlasmaTheme

class CommunityListItemUi : Ui<CommunityListItemUiState> {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    @Composable
    override fun Content(state: CommunityListItemUiState, modifier: Modifier) {
        val onEvent = state.onEvent

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = { onEvent(CommunityListItemEvent.OnClick) },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = state.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "38,789 members",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "4,590 new notes",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-12).dp),
                ) {
                    repeat(6) {
                        val randomSeed = (0..100).random()
                        Avatar(
                            imageUrl = "https://api.dicebear.com/6.x/avataaars/png?backgroundColor=b6e3f4,c0aede,d1d4f9&seed=$randomSeed",
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "light, normal")
@Preview(name = "dark, normal", uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "light, large", fontScale = 2f)
@Composable
private fun PreviewCommunityListItem() {
    PlasmaTheme {
        CommunityListItemUi().Content(
            state = CommunityListItemUiState(
                name = "#foodchain",
                onEvent = {},
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

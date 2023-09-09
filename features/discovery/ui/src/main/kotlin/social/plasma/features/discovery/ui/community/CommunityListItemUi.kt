package social.plasma.features.discovery.ui.community

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
                    verticalArrangement = Arrangement.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = state.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = state.trailingText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    state.captionText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                AvatarList(state.avatarList)
            }
        }
    }

    @Composable
    private fun AvatarList(avatars: List<String>) {
        Row(
            horizontalArrangement = Arrangement.spacedBy((-12).dp),
            modifier = Modifier
                .sizeIn(minHeight = 48.dp)
        ) {
            for (i in avatars.indices) {
                Avatar(
                    modifier = Modifier
                        .zIndex(-i.toFloat())
                        .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape),
                    imageUrl = avatars[i],
                    contentDescription = null
                )
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
                trailingText = "38,789 members",
                captionText = "4,590 new notes",
                avatarList = (1..6).map {
                    "https://api.dicebear.com/6.x/avataaars/png?backgroundColor=b6e3f4,c0aede,d1d4f9&seed=$it"
                },
                onEvent = {},
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(name = "light, no avatar")
@Composable
private fun PreviewCommunityListNoAvatar() {
    PlasmaTheme {
        CommunityListItemUi().Content(
            state = CommunityListItemUiState(
                name = "#foodchain",
                trailingText = "38,789 members",
                captionText = "4,590 new notes",
                avatarList = emptyList(),
                onEvent = {},
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

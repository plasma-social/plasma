package social.plasma.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import social.plasma.R
import social.plasma.ui.theme.PlasmaTheme

data class FeedCardUiModel(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val nip5: String?,
    val content: String,
    val timePosted: String,
    val replyCount: String,
    val shareCount: String,
    val likeCount: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedCard(
    uiModel: FeedCardUiModel,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
    ) {
        ListItem(
            leadingContent = uiModel.imageUrl?.let {
                {
                    Avatar(
                        imageUrl = it,
                        contentDescription = uiModel.name
                    )
                }
            },
            headlineText = { Text(uiModel.name) },
            supportingText = uiModel.nip5?.let {
                { Nip5Badge(it) }
            },
            trailingContent = { Text(uiModel.timePosted) },
        )

        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            text = uiModel.content
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { /*TODO*/ }) {
                Icon(painterResource(R.drawable.ic_plasma_replies), contentDescription = "")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = uiModel.replyCount)
            }

            TextButton(onClick = { /*TODO*/ }) {
                Icon(painterResource(R.drawable.ic_plasma_rocket_outline), contentDescription = "")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = uiModel.shareCount)
            }

            TextButton(onClick = { /*TODO*/ }) {
                Icon(painterResource(R.drawable.ic_plasma_shaka_outline), contentDescription = "")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = uiModel.likeCount)
            }
        }
    }
}

@Composable
private fun Nip5Badge(identifier: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // TODO Add badge
        Text(identifier)
    }
}

@Preview
@Composable
fun PreviewFeedCard() {
    PlasmaTheme {
        FeedCard(
            FeedCardUiModel(
                id = "id",
                name = "Pleb",
                nip5 = "nostrplebs.com",
                content = "Just a pleb doing pleb things. What’s your favorite nostr client, anon? \uD83E\uDD19",
                timePosted = "19m",
                imageUrl = null,
                replyCount = "352k",
                likeCount = "2.9M",
                shareCount = "509k"
            )
        )
    }
}
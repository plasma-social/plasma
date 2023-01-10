package social.plasma.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import social.plasma.R
import social.plasma.ui.theme.PlasmaTheme

data class FeedCardUiModel(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val nip5: String?,
    val content: String,
    val timePosted: String,
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
            leadingContent = {
                uiModel.imageUrl?.let {
                    AsyncImage(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(it)
                            .crossfade(true)
                            .build(),
                        contentScale = ContentScale.Crop,
                        contentDescription = ""
                    )
                }
            },
            headlineText = { Text(uiModel.name) },
            supportingText = { uiModel.nip5?.let { Text(it) } },
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
                Icon(painterResource(R.drawable.ic_plasma_bolt_outline), contentDescription = "")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "1.2K")
            }

            TextButton(onClick = { /*TODO*/ }) {
                Icon(painterResource(R.drawable.ic_plasma_rocket_outline), contentDescription = "")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "13")
            }

            TextButton(onClick = { /*TODO*/ }) {
                Icon(painterResource(R.drawable.ic_plasma_shaka_outline), contentDescription = "")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "42")
            }
        }
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
            )
        )
    }
}
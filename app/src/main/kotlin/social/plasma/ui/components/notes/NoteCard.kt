package social.plasma.ui.components.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.pager.ExperimentalPagerApi
import social.plasma.PubKey
import social.plasma.R
import social.plasma.ui.components.Avatar
import social.plasma.ui.components.ImageCarousel
import social.plasma.ui.components.Nip5Badge
import social.plasma.ui.components.ZoomableImage
import social.plasma.ui.components.notes.NoteUiModel.ContentBlock
import social.plasma.ui.theme.PlasmaTheme

@Composable
fun NoteElevatedCard(
    uiModel: NoteUiModel,
    modifier: Modifier = Modifier,
    onAvatarClick: ((PubKey) -> Unit)?,
    onLikeClick: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier,
    ) {
        NoteCardHeader(
            uiModel,
            onAvatarClick,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
        )
        NoteContent(uiModel, onLikeClick = onLikeClick)
    }
}

@Composable
fun NoteFlatCard(
    uiModel: NoteUiModel,
    modifier: Modifier = Modifier,
    onAvatarClick: ((PubKey) -> Unit)?,
    onLikeClick: () -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        NoteCardHeader(
            uiModel,
            onAvatarClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        NoteContent(uiModel, onLikeClick = onLikeClick)
    }
}

@Composable
fun ThreadNote(
    uiModel: NoteUiModel,
    modifier: Modifier = Modifier,
    onAvatarClick: ((PubKey) -> Unit)?,
    onLikeClick: () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        NoteCardHeader(
            uiModel,
            onAvatarClick,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
        )
        val borderColor = DividerDefaults.color
        val borderThickness = DividerDefaults.Thickness

        Column(
            modifier = Modifier
                .padding(start = 38.dp)
                .drawBehind {
                    drawLine(
                        borderColor,
                        Offset(0f, 0f),
                        Offset(0f, size.height),
                        borderThickness.toPx(),
                    )
                }
                .padding(start = 22.dp)
        ) {
            NoteContent(uiModel = uiModel, onLikeClick = onLikeClick)
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun NoteContent(
    uiModel: NoteUiModel,
    onLikeClick: () -> Unit,
) {
    FlowRow(
        modifier = Modifier.padding(16.dp),
    ) {
        uiModel.content.forEach {
            when (it) {
                is ContentBlock.Text -> {
                    Text(
                        text = it.text
                    )
                }

                is ContentBlock.Image -> {
                    ZoomableImage(
                        modifier = Modifier.fillMaxWidth(),
                        imageUrl = it.imageUrl
                    )
                }

                is ContentBlock.Carousel -> {
                    ImageCarousel(
                        modifier = Modifier.fillMaxWidth(),
                        imageUrls = it.imageUrls
                    )
                }

                is ContentBlock.Mention -> TODO()
            }
        }
    }

    NoteCardActionsRow(
        likeCount = uiModel.likeCount,
        replyCount = uiModel.replyCount,
        shareCount = uiModel.shareCount,
        isLiked = uiModel.isLiked,
        onLikeClick = onLikeClick,
    )
}

@Composable
private fun NoteCardActionsRow(
    likeCount: String,
    shareCount: String,
    replyCount: String,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val colors =
            ButtonDefaults.textButtonColors(
                // TODO figure out the right colors for like/unliked
                contentColor = MaterialTheme.colorScheme.primary.copy(alpha = .5f),
                disabledContentColor = MaterialTheme.colorScheme.primary
            )

        TextButton(
            onClick = { /*TODO*/ },
            colors = colors
        ) {
            Icon(painterResource(R.drawable.ic_plasma_replies), contentDescription = "")
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = replyCount, style = MaterialTheme.typography.labelMedium
            )
        }

        TextButton(
            onClick = { /*TODO*/ },
            colors = colors
        ) {
            Icon(painterResource(R.drawable.ic_plasma_rocket_outline), contentDescription = "")
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = shareCount,
                style = MaterialTheme.typography.labelMedium
            )
        }


        TextButton(
            onClick = onLikeClick,
            colors = colors,
            enabled = !isLiked
        ) {
            Icon(
                painterResource(R.drawable.ic_plasma_shaka_outline),
                contentDescription = "",
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = likeCount,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun NoteCardHeader(
    uiModel: NoteUiModel,
    onAvatarClick: ((PubKey) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
    ) {
        uiModel.avatarUrl?.let {
            Avatar(
                modifier = Modifier.padding(end = 16.dp),
                imageUrl = it,
                contentDescription = uiModel.name,
                onClick = onAvatarClick?.let { { onAvatarClick(uiModel.userPubkey) } }
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    uiModel.displayName,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = uiModel.timePosted,
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            uiModel.nip5?.let {
                Nip5Badge(it)
            }
        }

    }
    if (uiModel.cardLabel != null) {
        Text(
            text = uiModel.cardLabel,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Preview
@Composable
private fun PreviewFeedCard() {
    PlasmaTheme {
        NoteElevatedCard(
            NoteCardFakes.fakeUiModel,
            onAvatarClick = {},
            onLikeClick = {}
        )
    }
}

@Preview
@Composable
private fun PreviewThreadCard() {
    PlasmaTheme {
        ThreadNote(
            uiModel = NoteCardFakes.fakeUiModel,
            onAvatarClick = {},
            onLikeClick = {}
        )
    }
}

object NoteCardFakes {
    val fakeUiModel = NoteUiModel(
        id = "id",
        name = "@pleb",
        displayName = "Pleb",
        avatarUrl = "https://api.dicebear.com/5.x/bottts/jpg",
        nip5 = "nostrplebs.com",
        content = listOf(ContentBlock.Text("Just a pleb doing pleb things. What’s your favorite nostr client, anon? \uD83E\uDD19")),
        cardLabel = "Replying to Jack, JM, and 3 others",
        timePosted = "19m",
        replyCount = "352k",
        shareCount = "509k",
        likeCount = "2.9M",
        userPubkey = PubKey("fdsf")
    )
}

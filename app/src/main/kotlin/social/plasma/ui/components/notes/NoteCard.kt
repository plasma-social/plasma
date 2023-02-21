package social.plasma.ui.components.notes

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.pager.ExperimentalPagerApi
import social.plasma.R
import social.plasma.models.NoteId
import social.plasma.models.PubKey
import social.plasma.opengraph.OpenGraphMetadata
import social.plasma.ui.components.Avatar
import social.plasma.ui.components.ImageCarousel
import social.plasma.ui.components.InlineMediaPlayer
import social.plasma.ui.components.Nip5Badge
import social.plasma.ui.components.ZoomableImage
import social.plasma.ui.components.notes.NoteUiModel.ContentBlock
import social.plasma.ui.components.richtext.NoteMention
import social.plasma.ui.components.richtext.ProfileMention
import social.plasma.ui.components.richtext.RichText
import social.plasma.ui.theme.PlasmaTheme

typealias GetOpenGraphMetadata = suspend (String) -> OpenGraphMetadata?

@Composable
fun NoteElevatedCard(
    uiModel: NoteUiModel,
    modifier: Modifier = Modifier,
    onAvatarClick: (() -> Unit)?,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    getOpenGraphMetadata: GetOpenGraphMetadata,
    onProfileClick: (PubKey) -> Unit,
    onNoteClick: (NoteId) -> Unit,
) {
    ElevatedCard(
        modifier = modifier,
    ) {
        NoteCardHeader(
            uiModel,
            onAvatarClick,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
        )
        NoteContent(
            uiModel,
            onLikeClick = onLikeClick,
            onReplyClick = onReplyClick,
            getOpenGraphMetadata = getOpenGraphMetadata,
            onProfileClick = onProfileClick,
            onNoteClick = onNoteClick,
        )
    }
}

@Composable
fun NoteFlatCard(
    uiModel: NoteUiModel,
    modifier: Modifier = Modifier,
    onAvatarClick: (() -> Unit)?,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    getOpenGraphMetadata: GetOpenGraphMetadata,
    onProfileClick: (PubKey) -> Unit,
    onNoteClick: (NoteId) -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        NoteCardHeader(
            uiModel,
            onAvatarClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        NoteContent(
            uiModel,
            onLikeClick = onLikeClick,
            onReplyClick = onReplyClick,
            getOpenGraphMetadata = getOpenGraphMetadata,
            onProfileClick = onProfileClick,
            onNoteClick = onNoteClick
        )
    }
}

@Composable
fun ThreadNote(
    uiModel: NoteUiModel,
    modifier: Modifier = Modifier,
    onAvatarClick: (() -> Unit)?,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    showConnector: Boolean,
    getOpenGraphMetadata: GetOpenGraphMetadata,
    onProfileClick: (PubKey) -> Unit,
    onNoteClick: (NoteId) -> Unit,
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
                .then(
                    if (showConnector) {
                        Modifier.drawBehind {
                            drawLine(
                                borderColor,
                                Offset(0f, 0f),
                                Offset(0f, size.height),
                                borderThickness.toPx(),
                            )
                        }
                    } else Modifier
                )
                .padding(start = 22.dp)
        ) {
            NoteContent(
                uiModel = uiModel,
                onLikeClick = onLikeClick,
                onReplyClick = onReplyClick,
                getOpenGraphMetadata = getOpenGraphMetadata,
                onProfileClick = onProfileClick,
                onNoteClick = onNoteClick,
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun NoteContent(
    uiModel: NoteUiModel,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    getOpenGraphMetadata: GetOpenGraphMetadata,
    onProfileClick: (PubKey) -> Unit,
    onNoteClick: (NoteId) -> Unit,
) {
    if (uiModel.cardLabel != null) {
        Text(
            text = uiModel.cardLabel,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }

    FlowRow(
        modifier = Modifier
            .padding(16.dp)
            .animateContentSize(),
        mainAxisAlignment = FlowMainAxisAlignment.Start,
    ) {
        uiModel.content.forEach {
            when (it) {
                is ContentBlock.Text -> {
                    RichText(
                        text = it.content,
                        onMentionClick = { mention ->
                            when (mention) {
                                is NoteMention -> onNoteClick(mention.noteId)
                                is ProfileMention -> onProfileClick(mention.pubkey)
                            }
                        },
                    )
                }

                is ContentBlock.Image -> {
                    ZoomableImage(
                        modifier = Modifier.fillMaxWidth(),
                        imageUrl = it.imageUrl,
                        contentScale = ContentScale.FillWidth,
                    )
                }

                is ContentBlock.Carousel -> {
                    ImageCarousel(
                        modifier = Modifier.fillMaxWidth(),
                        imageUrls = it.imageUrls,
                    )
                }

                is ContentBlock.Video -> InlineMediaPlayer(it.videoUrl)
                is ContentBlock.UrlPreview -> OpenGraphPreviewCard(
                    it.url,
                    getOpenGraphMetadata,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }

    NoteCardActionsRow(
        likeCount = uiModel.likeCount,
        shareCount = uiModel.shareCount,
        replyCount = uiModel.replyCount,
        isLiked = uiModel.isLiked,
        onLikeClick = onLikeClick,
        onReplyClick = onReplyClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenGraphPreviewCard(
    url: String,
    getOpenGraphMetadata: GetOpenGraphMetadata,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var openGraphMetadata by remember { mutableStateOf<OpenGraphMetadata?>(null) }

    LaunchedEffect(url) {
        openGraphMetadata = getOpenGraphMetadata(url)
    }

    openGraphMetadata?.let { metadata ->
        OutlinedCard(
            modifier = modifier.clickable {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        ) {
            Column {
                metadata.image?.let {
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f),
                        contentScale = ContentScale.Crop,
                        model = it,
                        contentDescription = null,
                    )
                }
                ListItem(
                    headlineText = {
                        metadata.title?.let {
                            Text(it)
                        }
                    },
                    supportingText = {
                        metadata.description?.let {
                            Text(
                                it,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    overlineText = {
                        metadata.siteName?.let {
                            Text(it)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NoteCardActionsRow(
    likeCount: Int,
    shareCount: String,
    replyCount: String,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
) {
    val optimisticLikeState = remember(isLiked) { mutableStateOf(isLiked) }
    val optimisticLikeCount = remember(likeCount) { mutableStateOf(likeCount) }

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
            onClick = onReplyClick,
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
            onClick = {
                optimisticLikeState.value = true
                optimisticLikeCount.value += 1
                onLikeClick()
            },
            colors = colors,
            enabled = !optimisticLikeState.value
        ) {
            Icon(
                painterResource(R.drawable.ic_plasma_shaka_outline),
                contentDescription = "",
            )
            Spacer(modifier = Modifier.width(4.dp))

            val likeCountText =
                if (optimisticLikeCount.value > 0) "${optimisticLikeCount.value}" else ""
            Text(
                text = likeCountText,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun NoteCardHeader(
    uiModel: NoteUiModel,
    onAvatarClick: (() -> Unit)?,
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
                onClick = onAvatarClick?.let { { it() } }
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
}

@Preview
@Composable
private fun PreviewFeedCard() {
    PlasmaTheme {
        NoteElevatedCard(
            NoteCardFakes.fakeUiModel,
            onAvatarClick = {},
            onLikeClick = {},
            onReplyClick = {},
            getOpenGraphMetadata = { null },
            onNoteClick = {},
            onProfileClick = {}
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
            onLikeClick = {},
            onReplyClick = {},
            showConnector = true,
            getOpenGraphMetadata = { null },
            onNoteClick = {},
            onProfileClick = {}
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
        content = listOf(ContentBlock.Text(AnnotatedString("Just a pleb doing pleb things. What’s your favorite nostr client, anon? \uD83E\uDD19"))),
        cardLabel = "Replying to Jack, JM, and 3 others",
        timePosted = "19m",
        replyCount = "352k",
        shareCount = "509k",
        likeCount = 290,
        userPubkey = PubKey("fdsf")
    )
}

package social.plasma.features.feeds.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.cash.nostrino.crypto.PubKey
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.NavEvent
import social.plasma.features.feeds.screens.feed.ContentBlock
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.features.feeds.screens.feeditems.quotednotes.QuotedNoteScreen
import social.plasma.models.NoteId
import social.plasma.models.NoteMention
import social.plasma.models.ProfileMention
import social.plasma.ui.R
import social.plasma.ui.components.Avatar
import social.plasma.ui.components.ConfirmationDialog
import social.plasma.ui.components.GetOpenGraphMetadata
import social.plasma.ui.components.ImageCarousel
import social.plasma.ui.components.InlineMediaPlayer
import social.plasma.ui.components.MusicPlayer
import social.plasma.ui.components.Nip5Badge
import social.plasma.ui.components.OpenGraphPreviewCard
import social.plasma.ui.components.ZoomableImage
import social.plasma.ui.components.richtext.RichText
import social.plasma.ui.components.withHapticFeedBack
import social.plasma.ui.theme.PlasmaTheme


@Composable
fun NoteElevatedCard(
    uiModel: FeedItem.NoteCard,
    modifier: Modifier = Modifier,
    onAvatarClick: (() -> Unit)?,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onProfileClick: (PubKey) -> Unit,
    onNoteClick: (NoteId) -> Unit,
    onRepostClick: () -> Unit,
    getOpenGraphMetadata: GetOpenGraphMetadata,
    onHashTagClick: (String) -> Unit,
    onNestedNavEvent: (NavEvent) -> Unit = {},
    onZapClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    ) {
        NoteCardHeader(
            uiModel,
            onAvatarClick,
            onProfileClick = onProfileClick,
            onNoteClick = onNoteClick,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
            onHashTagClick = onHashTagClick,
        )
        NoteContent(
            uiModel,
            onProfileClick = onProfileClick,
            onNoteClick = onNoteClick,
            getOpenGraphMetadata = getOpenGraphMetadata,
            onHashTagClick = onHashTagClick,
            onNestedNavEvent = onNestedNavEvent,
        )
        NoteCardActionsRow(
            likeCount = uiModel.likeCount,
            shareCount = uiModel.shareCount,
            replyCount = uiModel.replyCount,
            isLiked = uiModel.isLiked,
            showZapButton = uiModel.zapsEnabled,
            onLikeClick = onLikeClick,
            onReplyClick = onReplyClick,
            onRepostClick = onRepostClick,
            onZapClick = onZapClick,
        )
    }
}

@Composable
fun EmbeddedNoteCard(
    uiModel: FeedItem.NoteCard,
    modifier: Modifier = Modifier,
    onNoteClick: (NoteId) -> Unit,
    onAvatarClick: (() -> Unit)?,
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    ) {
        Column {
            NoteCardHeader(
                uiModel,
                onAvatarClick,
                onProfileClick = { },
                onNoteClick = onNoteClick,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                onHashTagClick = { },
            )
            NoteContent(
                uiModel,
                onProfileClick = { },
                onNoteClick = onNoteClick,
                getOpenGraphMetadata = { null },
                onHashTagClick = { },
            )
        }
    }
}

@Composable
fun NoteFlatCard(
    uiModel: FeedItem.NoteCard,
    modifier: Modifier = Modifier,
    onAvatarClick: (() -> Unit)?,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onProfileClick: (PubKey) -> Unit,
    onNoteClick: (NoteId) -> Unit,
    onRepostClick: () -> Unit,
    getOpenGraphMetadata: GetOpenGraphMetadata,
    onHashTagClick: (String) -> Unit,
    onNestedNavEvent: (NavEvent) -> Unit = {},
    onZapClick: () -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        NoteCardHeader(
            uiModel,
            onAvatarClick,
            onProfileClick = onProfileClick,
            onNoteClick = onNoteClick,
            modifier = Modifier.padding(horizontal = 16.dp),
            onHashTagClick = onHashTagClick,
        )
        NoteContent(
            uiModel,
            onProfileClick = onProfileClick,
            onNoteClick = onNoteClick,
            getOpenGraphMetadata = getOpenGraphMetadata,
            onHashTagClick = onHashTagClick,
            onNestedNavEvent = onNestedNavEvent,
        )
        NoteCardActionsRow(
            likeCount = uiModel.likeCount,
            shareCount = uiModel.shareCount,
            replyCount = uiModel.replyCount,
            isLiked = uiModel.isLiked,
            onLikeClick = onLikeClick,
            onReplyClick = onReplyClick,
            onRepostClick = onRepostClick,
            showZapButton = uiModel.zapsEnabled,
            onZapClick = onZapClick
        )
    }
}

@Composable
fun ThreadNote(
    uiModel: FeedItem.NoteCard,
    modifier: Modifier = Modifier,
    onAvatarClick: (() -> Unit)?,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    showConnector: Boolean,
    onProfileClick: (PubKey) -> Unit,
    onNoteClick: (NoteId) -> Unit,
    onRepostClick: () -> Unit,
    getOpenGraphMetadata: GetOpenGraphMetadata,
    onHashTagClick: (String) -> Unit,
    onNestedNavEvent: (NavEvent) -> Unit = {},
    onZapClick: () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        NoteCardHeader(
            uiModel,
            onAvatarClick,
            onProfileClick = onProfileClick,
            onNoteClick = onNoteClick,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
            onHashTagClick = onHashTagClick,
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
                onProfileClick = onProfileClick,
                onNoteClick = onNoteClick,
                getOpenGraphMetadata = getOpenGraphMetadata,
                onHashTagClick = onHashTagClick,
                onNestedNavEvent = onNestedNavEvent,
            )
            NoteCardActionsRow(
                likeCount = uiModel.likeCount,
                shareCount = uiModel.shareCount,
                replyCount = uiModel.replyCount,
                isLiked = uiModel.isLiked,
                onLikeClick = onLikeClick,
                onReplyClick = onReplyClick,
                onRepostClick = onRepostClick,
                showZapButton = uiModel.zapsEnabled,
                onZapClick = onZapClick,
            )
        }
    }
}

@Composable
private fun NoteContent(
    uiModel: FeedItem.NoteCard,
    onProfileClick: (PubKey) -> Unit,
    onNoteClick: (NoteId) -> Unit,
    getOpenGraphMetadata: GetOpenGraphMetadata,
    onHashTagClick: (String) -> Unit,
    onNestedNavEvent: (NavEvent) -> Unit = {},
) {
    uiModel.cardLabel?.let {
        Text(
            text = it,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        uiModel.content.forEach {
            when (it) {
                is ContentBlock.Text -> {
                    RichText(
                        modifier = Modifier.fillMaxWidth(),
                        plainText = it.content,
                        onHashTagClick = onHashTagClick,
                        onMentionClick = { mention ->
                            when (mention) {
                                is NoteMention -> onNoteClick(mention.noteId)
                                is ProfileMention -> onProfileClick(mention.pubkey)
                            }
                        },
                        mentions = it.mentions,
                    )
                }

                is ContentBlock.Image -> {
                    ZoomableImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4 / 3f),
                        imageUrl = it.imageUrl,
                        contentScale = ContentScale.Crop,
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
                    modifier = Modifier.fillMaxWidth()
                )

                is ContentBlock.NoteQuote -> CircuitContent(
                    modifier = Modifier.fillMaxWidth(),
                    screen = QuotedNoteScreen(it.noteId),
                    onNavEvent = onNestedNavEvent
                )

                is ContentBlock.Audio -> OutlinedCard {
                    MusicPlayer(
                        audioUrl = it.audioUrl,
                        waveform = it.waveform,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                    )
                }
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
    showZapButton: Boolean,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onRepostClick: () -> Unit,
    onZapClick: () -> Unit,
) {
    val optimisticLikeState = remember(isLiked) { mutableStateOf(isLiked) }
    val optimisticLikeCount = remember(likeCount) { mutableStateOf(likeCount) }
    var showRepostAlert by remember { mutableStateOf(false) }
    val iconSize = remember { 20.dp }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val colors =
            ButtonDefaults.textButtonColors(
                // TODO figure out the right colors for like/unliked
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.primary
            )

        TextButton(
            onClick = onReplyClick,
            colors = colors
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(id = R.drawable.ic_plasma_replies),
                contentDescription = ""
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = replyCount, style = MaterialTheme.typography.labelMedium
            )
        }
        TextButton(
            onClick = { showRepostAlert = true },
            colors = colors
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(id = R.drawable.ic_plasma_rocket_outline),
                contentDescription = ""
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = shareCount,
                style = MaterialTheme.typography.labelMedium
            )
        }
        if (showZapButton) {
            TextButton(onClick = onZapClick, colors = colors) {
                Icon(
                    modifier = Modifier.size(iconSize),
                    painter = painterResource(id = R.drawable.ic_plasma_lightning_bolt),
                    contentDescription = null,
                )
                // TODO add zap count
            }
        }
        TextButton(
            onClick = withHapticFeedBack {
                optimisticLikeState.value = true
                optimisticLikeCount.value += 1
                onLikeClick()
            },
            colors = colors,
            enabled = !optimisticLikeState.value
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(id = R.drawable.ic_plasma_shaka_outline),
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

    if (showRepostAlert) {
        ConfirmationDialog(
            title = stringResource(R.string.repost_this_note),
            subtitle = stringResource(R.string.repost_notes_to_share_them_with_your_network),
            icon = painterResource(id = R.drawable.ic_plasma_rocket_outline),
            confirmLabel = stringResource(R.string.repost),
            onConfirm = withHapticFeedBack {
                showRepostAlert = false
                onRepostClick()
            },
            onDismiss = { showRepostAlert = false }
        )
    }
}

@Composable
private fun NoteCardHeader(
    uiModel: FeedItem.NoteCard,
    onAvatarClick: (() -> Unit)?,
    onProfileClick: (PubKey) -> Unit,
    onNoteClick: (NoteId) -> Unit,
    modifier: Modifier = Modifier,
    onHashTagClick: (String) -> Unit,
) {
    uiModel.headerContent?.let {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            RichText(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                plainText = it.content,
                mentions = it.mentions,
                onMentionClick = {
                    when (it) {
                        is ProfileMention -> onProfileClick(it.pubkey)
                        is NoteMention -> onNoteClick(it.noteId)
                    }
                },
                onHashTagClick = onHashTagClick,
            )
        }
    }

    Row(
        modifier = modifier,
    ) {
        Avatar(
            modifier = Modifier.padding(end = 16.dp),
            imageUrl = uiModel.avatarUrl,
            contentDescription = uiModel.name,
            onClick = onAvatarClick?.let { { it() } }
        )
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
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Nip5Badge(uiModel.nip5Status)
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
            onProfileClick = {},
            onNoteClick = {},
            onRepostClick = {},
            getOpenGraphMetadata = { null },
            onHashTagClick = {},
            onZapClick = {},
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
            onProfileClick = {},
            onNoteClick = {},
            onRepostClick = {},
            getOpenGraphMetadata = { null },
            onHashTagClick = {},
            onZapClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewEmbeddedCard() {
    PlasmaTheme {
        EmbeddedNoteCard(
            uiModel = NoteCardFakes.fakeUiModel,
            onAvatarClick = {},
            onNoteClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLoadingEmbeddedCard() {
    PlasmaTheme {
        EmbeddedNoteCard(
            uiModel = NoteCardFakes.fakeUiModel,
            onNoteClick = {},
            onAvatarClick = {},
        )
    }
}

object NoteCardFakes {
    val fakeUiModel = FeedItem.NoteCard(
        id = "id",
        name = "@pleb",
        displayName = "Pleb",
        avatarUrl = "https://api.dicebear.com/5.x/bottts/jpg",
        nip5Identifier = "nostrplebs.com",
        content = listOf(
            ContentBlock.Text(
                "Just a pleb doing pleb things. What’s your favorite nostr client, anon? \uD83E\uDD19",
                emptyMap(),
            )
        ),
        cardLabel = "Replying to Jack, JM, and 3 others",
        timePosted = "19m",
        replyCount = "352k",
        shareCount = "509k",
        likeCount = 290,
        userPubkey = PubKey.parse("npub1jem3jmdve9h94snjkuf5egagk7uupgxtu0eru33mzyms8ctzlk9sjhk73a"),
    )
}


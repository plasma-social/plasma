package social.plasma.features.feeds.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import social.plasma.features.feeds.screens.feed.ContentBlock
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.models.NoteId
import social.plasma.models.NoteMention
import social.plasma.models.ProfileMention
import social.plasma.ui.R
import social.plasma.ui.components.Avatar
import social.plasma.ui.components.ConfirmationDialog
import social.plasma.ui.components.GetOpenGraphMetadata
import social.plasma.ui.components.ImageCarousel
import social.plasma.ui.components.InlineMediaPlayer
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
            onLikeClick = onLikeClick,
            onReplyClick = onReplyClick,
            onProfileClick = onProfileClick,
            onNoteClick = onNoteClick,
            onRepostClick = onRepostClick,
            getOpenGraphMetadata = getOpenGraphMetadata,
            onHashTagClick = onHashTagClick
        )
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
            onLikeClick = onLikeClick,
            onReplyClick = onReplyClick,
            onProfileClick = onProfileClick,
            onNoteClick = onNoteClick,
            onRepostClick = onRepostClick,
            getOpenGraphMetadata = getOpenGraphMetadata,
            onHashTagClick = onHashTagClick
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
                onLikeClick = onLikeClick,
                onReplyClick = onReplyClick,
                onProfileClick = onProfileClick,
                onNoteClick = onNoteClick,
                onRepostClick = onRepostClick,
                getOpenGraphMetadata = getOpenGraphMetadata,
                onHashTagClick = onHashTagClick,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteContent(
    uiModel: FeedItem.NoteCard,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onProfileClick: (PubKey) -> Unit,
    onNoteClick: (NoteId) -> Unit,
    onRepostClick: () -> Unit,
    getOpenGraphMetadata: GetOpenGraphMetadata,
    onHashTagClick: (String) -> Unit,
) {
    uiModel.cardLabel?.let {
        Text(
            text = it,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    FlowRow(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        uiModel.content.forEach {
            when (it) {
                is ContentBlock.Text -> {
                    RichText(
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
        onRepostClick = onRepostClick,
    )
}

@Composable
private fun NoteCardActionsRow(
    likeCount: Int,
    shareCount: String,
    replyCount: String,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onRepostClick: () -> Unit,
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
    val isNip5Valid by produceState(initialValue = false, uiModel.nip5Identifier) {
        value = uiModel.isNip5Valid(uiModel.userPubkey, uiModel.nip5Identifier)
    }

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

            if (isNip5Valid) {
                uiModel.nip5Domain?.let {
                    Nip5Badge(it)
                }
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
            onProfileClick = {},
            onNoteClick = {},
            onRepostClick = {},
            getOpenGraphMetadata = { null },
            onHashTagClick = {},
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
        userPubkey = PubKey.parse("npub1jem3jmdve9h94snjkuf5egagk7uupgxtu0eru33mzyms8ctzlk9sjhk73a")
    )
}


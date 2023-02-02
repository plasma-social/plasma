package social.plasma.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.google.accompanist.pager.ExperimentalPagerApi
import social.plasma.PubKey
import social.plasma.R
import social.plasma.ui.components.NoteUiModel.RichContent
import social.plasma.ui.theme.PlasmaTheme

data class NoteUiModel(
    val id: String,
    val name: String,
    val displayName: String,
    val avatarUrl: String?,
    val nip5: String?,
    val content: String,
    val timePosted: String,
    val replyCount: String,
    val shareCount: String,
    val likeCount: String,
    val userPubkey: PubKey,
    val richContent: RichContent = RichContent.None,
) {
    sealed interface RichContent {
        object None : RichContent
        data class Image(val imageUrl: String) : RichContent

        data class Carousel(val imageUrls: List<String>) : RichContent
    }
}

@Composable
fun NoteElevatedCard(
    uiModel: NoteUiModel,
    modifier: Modifier = Modifier,
    onAvatarClick: ((PubKey) -> Unit)?,
) {
    ElevatedCard(
        modifier = modifier,
    ) {
        NoteCardHeader(
            uiModel,
            onAvatarClick,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
        )
        NoteContent(uiModel)
    }
}

@Composable
fun NoteFlatCard(
    uiModel: NoteUiModel,
    modifier: Modifier = Modifier,
    onAvatarClick: ((PubKey) -> Unit)?,
) {
    Column(
        modifier = modifier
    ) {
        NoteCardHeader(
            uiModel,
            onAvatarClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        NoteContent(uiModel)
    }
}

@Composable
fun ThreadNote(
    uiModel: NoteUiModel,
    modifier: Modifier = Modifier,
    onAvatarClick: ((PubKey) -> Unit)?,
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
            NoteContent(uiModel = uiModel)
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun NoteContent(
    uiModel: NoteUiModel,
) {
    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        text = uiModel.content
    )

    when (uiModel.richContent) {
        is RichContent.Image -> ZoomableImage(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            imageUrl = uiModel.richContent.imageUrl
        )

        is RichContent.Carousel -> ImageCarousel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            imageUrls = uiModel.richContent.imageUrls
        )

        RichContent.None -> {}
    }

    NoteCardActionsRow(
        likeCount = uiModel.likeCount,
        replyCount = uiModel.replyCount,
        shareCount = uiModel.shareCount,
    )
}

@Composable
private fun NoteCardActionsRow(
    likeCount: String,
    shareCount: String,
    replyCount: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = { /*TODO*/ }) {
            Icon(painterResource(R.drawable.ic_plasma_replies), contentDescription = "")
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = replyCount, style = MaterialTheme.typography.labelMedium
            )
        }

        TextButton(onClick = { /*TODO*/ }) {
            Icon(painterResource(R.drawable.ic_plasma_rocket_outline), contentDescription = "")
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = shareCount,
                style = MaterialTheme.typography.labelMedium
            )
        }

        TextButton(onClick = { /*TODO*/ }) {
            Icon(painterResource(R.drawable.ic_plasma_shaka_outline), contentDescription = "")
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    uiModel.displayName,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
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
            onAvatarClick = {}
        )
    }
}

@Preview
@Composable
private fun PreviewThreadCard() {
    PlasmaTheme {
        ThreadNote(uiModel = NoteCardFakes.fakeUiModel,
            onAvatarClick = {})
    }
}

object NoteCardFakes {
    val fakeUiModel = NoteUiModel(
        id = "id",
        name = "@pleb",
        displayName = "Pleb",
        avatarUrl = "https://api.dicebear.com/5.x/bottts/jpg",
        nip5 = "nostrplebs.com",
        content = "Just a pleb doing pleb things. What’s your favorite nostr client, anon? \uD83E\uDD19",
        timePosted = "19m",
        replyCount = "352k",
        shareCount = "509k",
        likeCount = "2.9M",
        userPubkey = PubKey("fdsf"),
        richContent = RichContent.None,
    )
}

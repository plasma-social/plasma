package social.plasma.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import social.plasma.PubKey
import social.plasma.R
import social.plasma.ui.theme.PlasmaTheme

data class NoteCardUiModel(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val nip5: String?,
    val content: String,
    val timePosted: String,
    val replyCount: String,
    val shareCount: String,
    val likeCount: String,
    val userPubkey: PubKey,
)

@Composable
fun NoteCard(
    uiModel: NoteCardUiModel,
    modifier: Modifier = Modifier,
    onAvatarClick: ((PubKey) -> Unit)?,
) {
    ElevatedCard(
        modifier = modifier,
    ) {
        NoteCardHeader(uiModel, onAvatarClick)

        NotContent(uiModel.content)

        NoteCardActionsRow(
            likeCount = uiModel.likeCount,
            replyCount = uiModel.replyCount,
            shareCount = uiModel.shareCount,
        )
    }
}

@Composable
private fun NotContent(content: String) {
    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        text = content
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
private fun NoteCardHeader(uiModel: NoteCardUiModel, onAvatarClick: ((PubKey) -> Unit)?) {
    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp),
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
                .padding(end = 16.dp)
        ) {
            Text(
                uiModel.name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            uiModel.nip5?.let {
                Nip5Badge(it)
            }
        }
        Text(
            text = uiModel.timePosted,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Preview
@Composable
private fun PreviewFeedCard() {
    PlasmaTheme {
        NoteCard(
            NoteCardUiModel(
                id = "id",
                name = "Pleb",
                nip5 = "nostrplebs.com",
                content = "Just a pleb doing pleb things. What’s your favorite nostr client, anon? \uD83E\uDD19",
                timePosted = "19m",
                avatarUrl = null,
                replyCount = "352k",
                likeCount = "2.9M",
                shareCount = "509k",
                userPubkey = PubKey("fdsf")
            ),
            onAvatarClick = {}
        )
    }
}

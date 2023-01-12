package social.plasma.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import social.plasma.R
import social.plasma.ui.components.Avatar
import social.plasma.ui.components.Nip5Badge
import social.plasma.ui.components.NoteCard
import social.plasma.ui.components.NoteCardUiModel
import social.plasma.ui.components.ProgressIndicator
import social.plasma.ui.components.StatCard
import social.plasma.ui.profile.ProfileUiState.Loaded.ProfileStat
import social.plasma.ui.profile.ProfileUiState.Loaded.UserData
import social.plasma.ui.theme.PlasmaTheme
import java.util.UUID

@Composable
fun Profile(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by profileViewModel.uiState.collectAsState(ProfileUiState.Loading)

    Profile(uiState = uiState, modifier = modifier)
}

@Composable
private fun Profile(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is ProfileUiState.Loading -> ProgressIndicator(modifier)
        is ProfileUiState.Loaded -> ProfileContent(uiState, modifier)
    }
}

@Composable
private fun ProfileContent(uiState: ProfileUiState.Loaded, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        item { ProfileBio(uiState.userData) }
        item { Spacer(modifier = Modifier.height(32.dp)) }

        item { ProfileStatsRow(uiState.statCards) }
        item { Spacer(modifier = Modifier.height(32.dp)) }

        items(uiState.feedNoteList) { cardUiModel ->
            NoteCard(uiModel = cardUiModel)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileBio(
    userData: UserData,
) {
    Column {
        Avatar(imageUrl = userData.avatarUrl, contentDescription = null)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Row {
                    Text(userData.petName, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(painterResource(R.drawable.ic_plasma_follow), contentDescription = null)
                }
                Text(userData.username, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /*TODO*/ }) {
                Icon(painterResource(R.drawable.ic_plasma_edit), null)
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(painterResource(R.drawable.ic_plasma_key), null)
            }
        }

        userData.nip5?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Nip5Badge(identifier = it)
        }

        Spacer(modifier = Modifier.height(8.dp))

        userData.bio?.let {
            Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProfileStatsRow(statCards: List<ProfileStat>) {
    Row {
        statCards.forEach { (label, value) ->
            StatCard(modifier = Modifier.weight(1f), label = label, value = value)
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PreviewProfile() {
    PlasmaTheme {
        Profile(FAKE_PROFILE)
    }
}

val FAKE_PROFILE = ProfileUiState.Loaded(
    userData = UserData(
        nip5 = "plasma.social",
        petName = "Satoshi",
        username = "@satoshi",
        publicKey = UUID.randomUUID().toString(),
        bio = "Developer @ a peer-to-peer electronic cash system",
        avatarUrl = "https://api.dicebear.com/5.x/bottts/jpg"
    ),
    feedNoteList = (0..20).map {
        NoteCardUiModel(
            id = "$it",
            name = "Satoshi",
            nip5 = "bitcoin.com",
            content = "Joplin and other markdown editors have pretty great UX these days \uD83E\uDD19",
            timePosted = "19m",
            imageUrl = null,
            replyCount = "352k",
            likeCount = "2.9M",
            shareCount = "509k"
        )
    },
    statCards = listOf(
        ProfileStat(
            label = "Followers",
            value = "2M",
        ),
        ProfileStat(
            label = "Following",
            value = "3.5k",
        ),
        ProfileStat(
            label = "Relays",
            value = "11",
        ),
    )
)
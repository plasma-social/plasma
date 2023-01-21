package social.plasma.ui.profile

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.coroutines.flow.flowOf
import social.plasma.models.PubKey
import java.util.UUID

class ProfilePreviewProvider : PreviewParameterProvider<ProfileUiState> {
    override val values = sequenceOf(
        createFakeProfile(),
        createFakeProfile(nip5 = null),
        createFakeProfile(username = null),
        createFakeProfile(username = null, nip5 = null),
        ProfileUiState.Loading,
    )

    private fun createFakeProfile(
        nip5: String? = "plasma.social",
        username: String? = "@satoshi",
    ): ProfileUiState.Loaded = ProfileUiState.Loaded(
        userNotesPagingFlow = flowOf(),
        statCards = listOf(
            ProfileUiState.Loaded.ProfileStat(
                label = "Followers",
                value = "2M",
            ),
            ProfileUiState.Loaded.ProfileStat(
                label = "Following",
                value = "3.5k",
            ),
            ProfileUiState.Loaded.ProfileStat(
                label = "Relays",
                value = "11",
            ),
        ),
        userData = ProfileUiState.Loaded.UserData(
            nip5 = nip5,
            petName = "Satoshi",
            username = username,
            publicKey = PubKey(UUID.randomUUID().toString()),
            about = "Developer @ a peer-to-peer electronic cash system",
            avatarUrl = "https://api.dicebear.com/5.x/bottts/jpg"
        )
    )
}

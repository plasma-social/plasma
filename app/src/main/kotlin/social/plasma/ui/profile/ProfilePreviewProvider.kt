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
            avatarUrl = "https://api.dicebear.com/5.x/bottts/jpg",
            website = "https://cash.app",
            lud = "lnurl",
            banner = "val nostrichImage =\n" +
                    "        \"https://s3-alpha-sig.figma.com/img/4a90/2d76/b5f9770952063fd97aa73441dbeef396?Expires=1675036800&Signature=isHUrgxr-OJjU4HHfA~wfa-GTLIq~FT83RxqEurf13bTXwLykd-aHhsMXuLhx2Zqs-g5hCj4jM3355ngZlcY9qcrcrTgwcAxZLbwAhpntHl499McE9BU7aO7jG7j~eMy0Z7a~p3lFCHuQsyO7ukKZsawWVkCNtPdl8E-IQ~yxMc~LAB6QSlQlEJV7hIwBAbWgOKDgQ6spq-UFeoOee5Po02JCGtZOEb9vlxzFrhBKdCxCh1PdrX0~9Qb8rEeLGzAFzhJeOKJ0RYwzHsiGYGWsc1Ad9nvgoCXY2FwwIrixsxh3Jy87BivV4XCibvTE7YHhXwTRY29D-0Yun95GsHWWw__&Key-Pair-Id=APKAQ4GOSFWCVNEHN3O4\""
        )
    )
}

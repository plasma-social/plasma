package social.plasma.features.profile.ui

import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.plasma.features.feeds.screens.feed.ContentBlock
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.features.feeds.screens.feed.FeedUiState
import social.plasma.features.feeds.ui.FeedUi
import social.plasma.features.profile.screens.ProfileUiState
import social.plasma.models.PubKey
import social.plasma.ui.testutils.TestDevice
import social.plasma.ui.testutils.TestFontScale
import social.plasma.ui.testutils.TestThemeConfig
import social.plasma.ui.theme.PlasmaTheme

@RunWith(TestParameterInjector::class)
internal class ProfileScreenUiTest(
    @TestParameter private val textSize: TestFontScale,
    @TestParameter private val themeVariation: TestThemeConfig,
    @TestParameter private val testDevice: TestDevice,
    @TestParameter(valuesProvider = ProfileTestCaseProvider::class) private val uiState: ProfileUiState,
) {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = testDevice.deviceConfig.copy(fontScale = textSize.scale),
    )

    @Test
    fun default() {
        paparazzi.snapshot(uiState)
    }

    private fun Paparazzi.snapshot(uiState: ProfileUiState) {
        snapshot {
            PlasmaTheme(
                darkTheme = themeVariation.isDarkTheme,
                dynamicStatusBar = false,
            ) {
                ProfileScreenUi(FeedUi()).Content(
                    state = uiState,
                    modifier = Modifier,
                )
            }
        }
    }
}


private class ProfileTestCaseProvider : TestParameter.TestParameterValuesProvider {
    override fun provideValues(): List<ProfileUiState> {
        val userData = ProfileUiState.Loaded.UserData(
            banner = "https://picsum.photos/id/866/800/600.jpg",
            website = "https://plasma.social",
            petName = "Plasma",
            username = "@plasma",
            publicKey = PubKey(""),
            about = "A native nostr client for Android",
            avatarUrl = "https://api.dicebear.com/5.x/big-smile/png?seed=plasma",
            nip5Identifier = "_@plasma.social",
            nip5Domain = "plasma.social",
            lud = "plasmasocial",
        )

        val statCards = listOf(
            ProfileUiState.Loaded.ProfileStat(
                label = "Following", value = "2.5k"
            ),
            ProfileUiState.Loaded.ProfileStat(
                label = "Followers", value = "13.9k"
            ),
            ProfileUiState.Loaded.ProfileStat(
                label = "Relays", value = "13"
            ),
        )
        val userNotesPagingFlow = flowOf<PagingData<FeedItem>>(PagingData.from((1..20).map {
            FeedItem.NoteCard(
                id = "$it",
                name = userData.username!!,
                displayName = userData.petName,
                avatarUrl = userData.avatarUrl,
                nip5Identifier = userData.nip5Identifier,
                content = listOf(
                    ContentBlock.Text(
                        "I find that there is a huge gap between what living artists create at their best and historical masterpieces displayed in museums, and that this gap can be closed through individual art patronage.",
                        emptyMap()
                    )
                ),
                cardLabel = null,
                timePosted = "5 min ago",
                replyCount = "55",
                shareCount = "500",
                likeCount = 400,
                userPubkey = PubKey(""),
                isLiked = true,
                isNip5Valid = { _, _ -> true },
                nip5Domain = userData.nip5Domain,
            )
        }))

        return listOf(
            ProfileUiState.Loaded(
                userData = userData,
                statCards = statCards,
                feedState = FeedUiState(
                    userNotesPagingFlow,
                    onEvent = {},
                    getOpenGraphMetadata = { null }),
                onEvent = {},
            )
        )
    }
}
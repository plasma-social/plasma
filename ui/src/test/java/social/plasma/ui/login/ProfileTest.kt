package social.plasma.ui.login

import androidx.paging.PagingData
import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_5
import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameter.TestParameterValuesProvider
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.plasma.models.PubKey
import social.plasma.ui.notes.NoteUiModel
import social.plasma.ui.notes.NoteUiModel.ContentBlock.Text
import social.plasma.ui.profile.Profile
import social.plasma.ui.profile.ProfileUiState
import social.plasma.ui.theme.PlasmaTheme
import social.plasma.ui.util.AccessibilityTextSize
import social.plasma.ui.util.ThemeVariation

@RunWith(TestParameterInjector::class)
internal class ProfileTest(
    @TestParameter private val textSize: AccessibilityTextSize,
    @TestParameter private val themeVariation: ThemeVariation,
    @TestParameter(valuesProvider = ProfileTestCaseProvider::class) private val uiState: ProfileUiState,
) {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = PIXEL_5.copy(fontScale = textSize.scale),
    )

    @Test
    fun default() {
        paparazzi.snapshot(uiState)
    }

    private fun Paparazzi.snapshot(uiState: ProfileUiState) {
        snapshot {
            PlasmaTheme(
                darkTheme = themeVariation == ThemeVariation.Dark,
                dynamicStatusBar = false,
            ) {
                Profile(
                    uiState = uiState,
                    onNoteDisposed = {},
                    onNoteDisplayed = {},
                    onNavigateBack = { },
                    onNoteClick = { },
                    onNoteReaction = { },
                    onReply = { },
                    getOpenGraphMetadata = { null },
                    onNavigateToProfile = { },
                    onRepostClick = { },
                )
            }
        }
    }
}

private class ProfileTestCaseProvider : TestParameterValuesProvider {
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
        val userNotesPagingFlow = flowOf(PagingData.from((1..20).map {
            NoteUiModel(
                id = "$it",
                name = userData.username!!,
                displayName = userData.petName,
                avatarUrl = userData.avatarUrl,
                nip5Identifier = userData.nip5Identifier,
                content = listOf(
                    Text(
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
                isNip5Valid = { _, _ -> true },
                userNotesPagingFlow = userNotesPagingFlow
            )
        )
    }
}

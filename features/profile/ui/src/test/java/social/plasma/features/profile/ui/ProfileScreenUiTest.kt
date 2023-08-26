package social.plasma.features.profile.ui

import androidx.compose.ui.Modifier
import app.cash.nostrino.crypto.PubKey
import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.slack.circuit.overlay.ContentWithOverlays
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.plasma.features.feeds.screens.eventfeed.EventFeedUiState
import social.plasma.features.profile.screens.ProfileUiState
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
                ContentWithOverlays {
                    ProfileScreenUi().Content(
                        state = uiState,
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}


private class ProfileTestCaseProvider : TestParameter.TestParameterValuesProvider {
    override fun provideValues(): List<ProfileUiState> {
        val userData = ProfileUiState.Loaded.UserData(
            banner = "https://picsum.photos/id/866/800/600.jpg",
            website = "https://plasma.social",
            displayName = "qwertyuiopasdf".repeat(5),
            username = "@plasma",
            publicKey = PubKey.parse("npub180cvv07tjdrrgpa0j7j7tmnyl2yr6yr7l8j4s3evf6u64th6gkwsyjh6w6"),
            about = "A native nostr client for Android",
            avatarUrl = "https://api.dicebear.com/5.x/big-smile/png?seed=plasma",
            nip5Identifier = "_@plasma.social",
            nip5Domain = "plasma.social",
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

        return listOf(
            ProfileUiState.Loaded(
                userData = userData,
                statCards = statCards,
                showLightningIcon = true,
                feedState = EventFeedUiState.Empty,
                onEvent = {},
            )
        )
    }
}

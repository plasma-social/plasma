package social.plasma.features.feeds.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import app.cash.nostrino.crypto.PubKey
import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import okio.ByteString.Companion.decodeHex
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.plasma.features.feeds.screens.feed.ContentBlock
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.features.feeds.ui.notes.EmbeddedNoteCard
import social.plasma.features.feeds.ui.notes.NoteElevatedCard
import social.plasma.ui.testutils.TestDevice
import social.plasma.ui.testutils.TestFontScale
import social.plasma.ui.testutils.TestThemeConfig
import social.plasma.ui.theme.PlasmaTheme

@RunWith(TestParameterInjector::class)
internal class NoteCardTest(
    @TestParameter private val textSize: TestFontScale,
    @TestParameter private val themeVariation: TestThemeConfig,
    @TestParameter private val testDevice: TestDevice,
) {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = testDevice.deviceConfig.copy(fontScale = textSize.scale),
    )

    @Test
    fun `note card`() {
        snapshot {
            NoteElevatedCard(
                uiModel = uiModel,
                onAvatarClick = null,
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

    @Test
    fun `embedded note card`() {
        snapshot {
            EmbeddedNoteCard(
                uiModel = uiModel,
                onNoteClick = {},
                onAvatarClick = {}
            )
        }
    }

    private fun snapshot(content: @Composable () -> Unit) {
        paparazzi.snapshot {
            PlasmaTheme(
                darkTheme = themeVariation.isDarkTheme,
                dynamicStatusBar = false,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    content()
                }
            }
        }
    }

    companion object {
        private val uiModel = FeedItem.NoteCard(
            id = "",
            name = "Jane",
            displayName = "Jane",
            headerContent = ContentBlock.Text(
                "Boosted by Iris",
                emptyMap()
            ),
            content = listOf(ContentBlock.Text("PV", emptyMap())),
            cardLabel = "Replying to Mike",
            timePosted = "Just now",
            userPubkey = PubKey("9c9ecd7c8a8c3144ae48bf425b6592c8e53c385fd83376d4ffb7f6ac1a17bfab".decodeHex()),
            zapsEnabled = true,
        )

    }
}



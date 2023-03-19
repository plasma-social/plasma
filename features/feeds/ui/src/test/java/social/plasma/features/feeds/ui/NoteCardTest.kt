package social.plasma.features.feeds.ui

import androidx.compose.foundation.layout.Box
import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.plasma.features.feeds.screens.feed.ContentBlock
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.features.feeds.ui.notes.NoteElevatedCard
import social.plasma.models.PubKey
import social.plasma.ui.testutils.TestDevice
import social.plasma.ui.testutils.TestFontScale
import social.plasma.ui.testutils.TestThemeConfig
import social.plasma.ui.theme.PlasmaTheme

enum class NoteCardTestCase(val uiModel: FeedItem.NoteCard) {
    Repost(
        FeedItem.NoteCard(
            headerContent = ContentBlock.Text(
                "Boosted by Iris",
                emptyMap()
            ),
            timePosted = "Just now",
            cardLabel = "Replying to Mike",
            name = "Jane",
            displayName = "Jane",
            id = "",
            content = listOf(ContentBlock.Text("PV", emptyMap())),
            userPubkey = PubKey("9c9ecd7c8a8c3144ae48bf425b6592c8e53c385fd83376d4ffb7f6ac1a17bfab")
        )
    )
}

@RunWith(TestParameterInjector::class)
internal class NoteCardTest(
    @TestParameter private val textSize: TestFontScale,
    @TestParameter private val themeVariation: TestThemeConfig,
    @TestParameter private val testDevice: TestDevice,
    @TestParameter private val cardTestCase: NoteCardTestCase,
) {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = testDevice.deviceConfig.copy(fontScale = textSize.scale),
    )

    @Test
    fun `note card`() {
        paparazzi.snapshot(cardTestCase.uiModel)
    }

    private fun Paparazzi.snapshot(model: FeedItem.NoteCard) {
        snapshot {
            PlasmaTheme(
                darkTheme = themeVariation.isDarkTheme,
                dynamicStatusBar = false,
            ) {
                Box {
                    NoteElevatedCard(
                        uiModel = model,
                        onAvatarClick = null,
                        onLikeClick = {},
                        onReplyClick = {},
                        onProfileClick = {},
                        onNoteClick = {},
                        onRepostClick = {},
                    )
                }
            }
        }
    }
}



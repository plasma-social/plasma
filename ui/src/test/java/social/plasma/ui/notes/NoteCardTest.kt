package social.plasma.ui.notes

import androidx.compose.foundation.layout.Box
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.plasma.models.PubKey
import social.plasma.ui.theme.PlasmaTheme
import social.plasma.ui.util.AccessibilityTextSize
import social.plasma.ui.util.ThemeVariation

enum class NoteCardTestCase(val uiModel: NoteUiModel) {
    Repost(
        NoteUiModel(
            headerContent = NoteUiModel.ContentBlock.Text(
                "Boosted by Iris",
                emptyMap()
            ),
            timePosted = "Just now",
            cardLabel = "Replying to Mike",
            name = "Jane",
            displayName = "Jane",
            id = "",
            content = listOf(NoteUiModel.ContentBlock.Text("PV", emptyMap())),
            userPubkey = PubKey("9c9ecd7c8a8c3144ae48bf425b6592c8e53c385fd83376d4ffb7f6ac1a17bfab")
        )
    )
}

@RunWith(TestParameterInjector::class)
internal class NoteCardTest(
    @TestParameter private val textSize: AccessibilityTextSize,
    @TestParameter private val themeVariation: ThemeVariation,
    @TestParameter private val cardTestCase: NoteCardTestCase,
) {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(fontScale = textSize.scale),
    )

    @Test
    fun `note card`() {
        paparazzi.snapshot(cardTestCase.uiModel)
    }

    private fun Paparazzi.snapshot(noteUiModel: NoteUiModel) {
        snapshot {
            PlasmaTheme(
                darkTheme = themeVariation == ThemeVariation.Dark,
                dynamicStatusBar = false,
            ) {
                Box {
                    NoteElevatedCard(
                        uiModel = noteUiModel,
                        onAvatarClick = null,
                        onLikeClick = {},
                        onReplyClick = {},
                        getOpenGraphMetadata = { null },
                        onProfileClick = {},
                        onNoteClick = {},
                        onRepostClick = {}
                    )
                }
            }
        }
    }
}



package social.plasma.features.posting.ui.composepost

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.plasma.features.posting.screens.ComposePostUiState
import social.plasma.models.PubKey
import social.plasma.models.TagSuggestion
import social.plasma.ui.testutils.TestDevice
import social.plasma.ui.testutils.TestFontScale
import social.plasma.ui.testutils.TestThemeConfig
import social.plasma.ui.theme.PlasmaTheme

@RunWith(TestParameterInjector::class)
class ComposePostUiTest constructor(
    @TestParameter testDevice: TestDevice,
    @TestParameter private val themeVariation: TestThemeConfig,
    @TestParameter fontScale: TestFontScale,
) {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = testDevice.deviceConfig.copy(fontScale = fontScale.scale),
    )

    @Test
    fun default() {
        snapshot(
            ComposePostUiState(
                postButtonLabel = "Post",
                placeholder = "What's zappening?",
                postButtonEnabled = false,
                title = "Create Post",
                noteContent = TextFieldValue(""),
            ) {}
        )
    }

    @Test
    fun `button enabled`() {
        snapshot(
            ComposePostUiState(
                postButtonLabel = "Post",
                placeholder = "What's zappening?",
                postButtonEnabled = true,
                title = "Create Post",
                noteContent = TextFieldValue("Testing with content")
            ) {}
        )
    }

    @Test
    fun `displaying tag suggestions`() {
        snapshot(
            ComposePostUiState(
                postButtonLabel = "Post",
                placeholder = "What's zappening?",
                postButtonEnabled = true,
                title = "Create Post",
                showTagSuggestions = true,
                noteContent = TextFieldValue("@ko"),
                tagSuggestions = listOf(
                    TagSuggestion(
                        pubKey = PubKey("1"),
                        imageUrl = null,
                        title = "KoalaSat",
                        subtitle = "koalasat@nostros.net",
                    ),
                    TagSuggestion(
                        pubKey = PubKey("1"),
                        imageUrl = null,
                        title = "KoalaSat",
                        subtitle = "koalasat@nostros.net",
                    ),
                    TagSuggestion(
                        pubKey = PubKey("1"),
                        imageUrl = null,
                        title = "KoalaSat",
                        subtitle = "koalasat@nostros.net",
                    ),

                    )
            ) {}
        )
    }

    private fun snapshot(uiState: ComposePostUiState) {
        paparazzi.snapshot {
            PlasmaTheme(darkTheme = themeVariation.isDarkTheme) {
                ComposePostUi().Content(
                    state = uiState,
                    modifier = Modifier,
                )
            }
        }
    }
}

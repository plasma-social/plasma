package social.plasma.features.discovery.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.plasma.features.discovery.screens.communities.CommunityListItemUiState
import social.plasma.ui.testutils.TestDevice
import social.plasma.ui.testutils.TestFontScale
import social.plasma.ui.testutils.TestThemeConfig
import social.plasma.ui.theme.PlasmaTheme


@RunWith(TestParameterInjector::class)
class CommunityListItemUiTest constructor(
    @TestParameter testDevice: TestDevice,
    @TestParameter private val themeVariation: TestThemeConfig,
    @TestParameter fontScale: TestFontScale,
) {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = testDevice.deviceConfig.copy(fontScale = fontScale.scale),
    )

    @Test
    fun test() {
        snapshot(CommunityListItemUiState(name = "#aVeryLongHashtag") {})
    }

    private fun snapshot(uiState: CommunityListItemUiState) {
        paparazzi.snapshot {
            PlasmaTheme(darkTheme = themeVariation.isDarkTheme) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    CommunityListItemUi().Content(
                        state = uiState,
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}

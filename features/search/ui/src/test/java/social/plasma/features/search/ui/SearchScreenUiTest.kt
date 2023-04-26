package social.plasma.features.search.ui

import androidx.compose.ui.Modifier
import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.plasma.features.search.screens.SearchUiState
import social.plasma.ui.testutils.TestDevice
import social.plasma.ui.testutils.TestFontScale
import social.plasma.ui.testutils.TestThemeConfig
import social.plasma.ui.theme.PlasmaTheme

@RunWith(TestParameterInjector::class)
internal class SearchScreenUiTest(
    @TestParameter private val textSize: TestFontScale,
    @TestParameter private val themeVariation: TestThemeConfig,
    @TestParameter private val testDevice: TestDevice,
    @TestParameter(valuesProvider = SearchScreenTestValuesProvider::class) private val uiState: SearchUiState,
) {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = testDevice.deviceConfig.copy(fontScale = textSize.scale),
    )

    @Test
    fun snapshot() {
        paparazzi.snapshot(uiState)
    }

    private fun Paparazzi.snapshot(loginState: SearchUiState) {
        snapshot {
            PlasmaTheme(
                darkTheme = themeVariation.isDarkTheme,
                dynamicStatusBar = false,
            ) {
                SearchScreenUi().Content(
                    state = loginState,
                    modifier = Modifier
                )
            }
        }
    }
}

private class SearchScreenTestValuesProvider : TestParameter.TestParameterValuesProvider {
    private val searchScreenPreviewProvider = SearchScreenPreviewProvider()
    override fun provideValues(): List<SearchUiState> = searchScreenPreviewProvider.values.toList()
}

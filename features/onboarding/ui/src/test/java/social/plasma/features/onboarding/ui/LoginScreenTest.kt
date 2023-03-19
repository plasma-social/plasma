package social.plasma.features.onboarding.ui

import androidx.compose.ui.Modifier
import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.plasma.features.onboarding.screens.login.LoginUiState
import social.plasma.features.onboarding.ui.login.LoginScreenUi
import social.plasma.ui.testutils.TestDevice
import social.plasma.ui.testutils.TestFontScale
import social.plasma.ui.testutils.TestThemeConfig
import social.plasma.ui.theme.PlasmaTheme

@RunWith(TestParameterInjector::class)
internal class LoginScreenTest(
    @TestParameter private val textSize: TestFontScale,
    @TestParameter private val themeVariation: TestThemeConfig,
    @TestParameter private val testDevice: TestDevice,
) {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = testDevice.deviceConfig.copy(fontScale = textSize.scale),
    )

    @Test
    fun default() {
        paparazzi.snapshot(
            LoginUiState(
                keyInput = "",
                loginButtonVisible = false,
                clearInputButtonVisible = false,
                onEvent = {}
            )
        )
    }

    @Test
    fun `with input`() {
        paparazzi.snapshot(
            LoginUiState(
                keyInput = "npublkjfdslkfjsdlkfjdlskfjlsdkajflsdkajflkj432l5k2j435lkjlkj543lk5jl43kj5",
                loginButtonVisible = true,
                clearInputButtonVisible = true,
                onEvent = {}
            )
        )
    }

    private fun Paparazzi.snapshot(loginState: LoginUiState) {
        snapshot {
            PlasmaTheme(
                darkTheme = themeVariation.isDarkTheme,
                dynamicStatusBar = false,
            ) {
                LoginScreenUi().Content(
                    state = loginState,
                    modifier = Modifier
                )
            }
        }
    }
}

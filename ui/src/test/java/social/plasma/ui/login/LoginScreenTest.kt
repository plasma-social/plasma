package social.plasma.ui.login

import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_5
import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.plasma.ui.theme.PlasmaTheme
import social.plasma.ui.util.AccessibilityTextSize
import social.plasma.ui.util.ThemeVariation

@RunWith(TestParameterInjector::class)
internal class LoginScreenTest(
    @TestParameter private val textSize: AccessibilityTextSize,
    @TestParameter private val themeVariation: ThemeVariation,
) {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = PIXEL_5.copy(fontScale = textSize.scale),
    )

    @Test
    fun default() {
        paparazzi.snapshot(
            LoginState.LoggedOut(
                keyInput = "",
                loginButtonVisible = false,
                clearInputButtonVisible = false
            )
        )
    }

    @Test
    fun `with input`() {
        paparazzi.snapshot(
            LoginState.LoggedOut(
                keyInput = "npublkjfdslkfjsdlkfjdlskfjlsdkajflsdkajflkj432l5k2j435lkjlkj543lk5jl43kj5",
                loginButtonVisible = true,
                clearInputButtonVisible = true
            )
        )
    }

    private fun Paparazzi.snapshot(loginState: LoginState.LoggedOut) {
        snapshot {
            PlasmaTheme(
                darkTheme = themeVariation == ThemeVariation.Dark,
                dynamicStatusBar = false,
            ) {
                LoginScreen(
                    uiState = loginState,
                    onKeyInputChanged = {},
                    onLoginButtonClick = {},
                )
            }
        }
    }
}

package social.plasma.ui.components

import androidx.compose.ui.res.painterResource
import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.plasma.ui.R
import social.plasma.ui.testutils.TestDevice
import social.plasma.ui.testutils.TestFontScale
import social.plasma.ui.testutils.TestThemeConfig
import social.plasma.ui.theme.PlasmaTheme

@RunWith(TestParameterInjector::class)
internal class AlertDialogsTest(
    @TestParameter private val textSize: TestFontScale,
    @TestParameter private val themeVariation: TestThemeConfig,
    @TestParameter private val testDevice: TestDevice,
) {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = testDevice.deviceConfig.copy(fontScale = textSize.scale),
    )


    @Test
    fun `boost confirmation dialog`() {
        paparazzi.snapshot {
            PlasmaTheme(darkTheme = themeVariation.isDarkTheme) {
                ConfirmationDialog(
                    title = "Boost this note",
                    subtitle = "Boost this note to increase its reach across the network",
                    icon = painterResource(id = R.drawable.ic_plasma_rocket_outline),
                    confirmLabel = "Boost",
                    onConfirm = {},
                    onDismiss = {})
            }
        }
    }
}

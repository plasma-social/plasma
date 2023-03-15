package social.plasma.ui.components

import androidx.compose.ui.res.painterResource
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.plasma.ui.R
import social.plasma.ui.theme.PlasmaTheme
import social.plasma.ui.util.AccessibilityTextSize
import social.plasma.ui.util.ThemeVariation

@RunWith(TestParameterInjector::class)
internal class AlertDialogsTest(
    @TestParameter private val textSize: AccessibilityTextSize,
    @TestParameter private val themeVariation: ThemeVariation,
) {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(fontScale = textSize.scale),
    )
    private val isDarkTheme = themeVariation == ThemeVariation.Dark


    @Test
    fun `boost confirmation dialog`() {
        paparazzi.snapshot {
            PlasmaTheme(darkTheme = isDarkTheme) {
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

package social.plasma.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.plasma.ui.testutils.TestDevice
import social.plasma.ui.testutils.TestFontScale
import social.plasma.ui.testutils.TestThemeConfig
import social.plasma.ui.theme.PlasmaTheme

@RunWith(TestParameterInjector::class)
internal class SelectZapAmountComponentTest(
    @TestParameter private val textSize: TestFontScale,
    @TestParameter private val themeVariation: TestThemeConfig,
    @TestParameter private val testDevice: TestDevice,
) {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = testDevice.deviceConfig.copy(fontScale = textSize.scale),
    )

    @Test
    fun `select zap amount dialog`() = paparazzi.snapshot {
        PlasmaTheme(darkTheme = themeVariation.isDarkTheme) {
            val model = SelectZapAmountModel(
                amountBuckets = listOf(
                    21L,
                    42L,
                    69L,
                    100L,
                    420L,
                    1000L,
                    10000L
                )
            )
            SelectZapAmountComponent(modifier = Modifier.padding(16.dp), model = model) {}
        }
    }
}

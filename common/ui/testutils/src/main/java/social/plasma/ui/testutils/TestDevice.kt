package social.plasma.ui.testutils
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_5


enum class TestDevice(
    val deviceConfig: DeviceConfig
) {
    Pixel5(PIXEL_5)
}
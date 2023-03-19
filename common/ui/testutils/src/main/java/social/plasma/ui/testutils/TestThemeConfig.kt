package social.plasma.ui.testutils


enum class TestThemeConfig(
    val isDarkTheme: Boolean
) {
    DarkTheme(isDarkTheme = true),
    LightTheme(isDarkTheme = false),
}
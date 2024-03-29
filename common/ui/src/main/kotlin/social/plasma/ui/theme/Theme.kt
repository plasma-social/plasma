package social.plasma.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = with(DarkThemeColors) {
    darkColorScheme(
        background = fillPage,
        surface = fillElevation,
        surfaceVariant = fillElevation,
        onSurface = textPrimary,
        onSurfaceVariant = textHint,
        primary = fillPrimary,
        onPrimary = onPrimary,
        onSecondary = textSecondary,
        outline = strokesAndDividersDefault,
    )
}

private val LightColorScheme = with(LightThemeColors) {
    lightColorScheme(
        background = fillPage,
        surface = fillElevation,
        surfaceVariant = fillElevation,
        onSurface = textPrimary,
        onSurfaceVariant = textHint,
        primary = fillPrimary,
        onPrimary = onPrimary,
        onSecondary = textSecondary,
        outline = strokesAndDividersDefault,
    )
}

@Composable
fun PlasmaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    dynamicStatusBar: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (dynamicStatusBar && !view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

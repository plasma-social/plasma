package social.plasma.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

val FigmaStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        LineHeightStyle.Alignment.Center,
        LineHeightStyle.Trim.None,
    )
)

// Set of Material typography styles to start with
val Typography = Typography(
    //AG: Paragraph
    bodyLarge = FigmaStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,

        ),
    // AG: Display
    displaySmall = FigmaStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 40.sp
    ),
    // AG: Paragraph Tiny
    bodySmall = FigmaStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    // This is the style that toolbar titles use by default
    // It's the same as AG: Title
    titleLarge = FigmaStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    // AG: Title
    titleMedium = FigmaStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    // AG: Detail
    labelMedium = FigmaStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelLarge = FigmaStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
    )
)

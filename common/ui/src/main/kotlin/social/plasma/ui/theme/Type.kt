package social.plasma.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import social.plasma.ui.R


private val fontFam = FontFamily(
    Font(R.font.plex_sans_bold, FontWeight.Bold),
    Font(R.font.plex_sans_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.plex_sans_extra_light, FontWeight.ExtraLight),
    Font(R.font.plex_sans_extra_light_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.plex_sans_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.plex_sans_light, FontWeight.Light),
    Font(R.font.plex_sans_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.plex_sans_medium, FontWeight.Medium),
    Font(R.font.plex_sans_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.plex_sans_regular, FontWeight.Normal),
    Font(R.font.plex_sans_semi_bold, FontWeight.SemiBold),
    Font(R.font.plex_sans_semi_bold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.plex_sans_thin, FontWeight.Thin),
    Font(R.font.plex_sans_thin_italic, FontWeight.Thin, FontStyle.Italic),
)


val FigmaStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        LineHeightStyle.Alignment.Center,
        LineHeightStyle.Trim.None,
    ),
    fontFamily = fontFam,
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
    // AG: Detail Heavy
    // Used for tab labels
    titleSmall = FigmaStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
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
    // AG: Section
    labelSmall = FigmaStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 18.sp,
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
    ),
)

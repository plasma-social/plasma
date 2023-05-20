package social.plasma.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * A composable that wraps a block with haptic feedback.
 * Useful for buttons and other clickable elements.
 */
@Composable
inline fun withHapticFeedBack(
    feedbackType: HapticFeedbackType = HapticFeedbackType.LongPress,
    crossinline block: () -> Unit
): () -> Unit {
    val hapticFeedBack = LocalHapticFeedback.current

    return {
        block()
        hapticFeedBack.performHapticFeedback(feedbackType)
    }
}

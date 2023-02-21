package social.plasma.ui.components.richtext

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.consumeDownChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeConsumed
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastAny
import social.plasma.models.NoteId
import social.plasma.models.PubKey
import social.plasma.ui.components.notes.RichTextParser
import kotlin.math.ceil

object RichTextTag {
    const val URL = "URL"
    const val PROFILE = "PROFILE"
    const val NOTE = "NOTE"
}

@Composable
fun RichText(
    plainText: String,
    mentions: Map<Int, Mention>,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = TextStyle.Default,
    onMentionClick: (Mention) -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    val linkColor = MaterialTheme.colorScheme.primary
    val parser = remember(linkColor) { RichTextParser(linkColor) }

    var richText by remember(plainText) {
        mutableStateOf(AnnotatedString(""))
    }

    LaunchedEffect(plainText) {
        richText = parser.parse(plainText, mentions)
    }

    fun handleClick(tag: String, item: String) {
        when (tag) {
            RichTextTag.URL -> uriHandler.openUri(item)
            RichTextTag.PROFILE -> onMentionClick(ProfileMention(pubkey = PubKey(item), text = ""))
            RichTextTag.NOTE -> onMentionClick(NoteMention(noteId = NoteId(item), text = ""))
            else -> error("Unsupported tag: $tag")
        }
    }

    val linkAnnotations = richText.getStringAnnotations(0, richText.length)

    if (linkAnnotations.isNotEmpty()) {
        var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
        val linkify = Modifier.linkify(linkAnnotations, { layoutResult }) { tag, item ->
            handleClick(tag, item)
        }

        Text(
            modifier = modifier
                .then(linkify),
            text = richText,
            style = style,
            textAlign = textAlign,
            color = color,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            onTextLayout = {
                layoutResult = it
                onTextLayout(it)
            },
            inlineContent = emptyMap(),
        )
    } else {
        Text(
            modifier = modifier,
            text = richText,
            style = style,
            textAlign = textAlign,
            color = color,
            onTextLayout = onTextLayout,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            inlineContent = emptyMap(),
        )
    }
}

/**
 * Consumes tap events, but only on the links themselves.
 */
private fun Modifier.linkify(
    linkAnnotations: List<AnnotatedString.Range<String>>,
    layoutResult: () -> TextLayoutResult?,
    onItemClick: (String, String) -> Unit,
): Modifier {
    val drawTappableBounds = if (ShowDebugBounds) {
        Modifier.drawDebugBounds(linkAnnotations, layoutResult)
    } else {
        Modifier
    }
    val clicks = Modifier.pointerInput(linkAnnotations, onItemClick) {
        forEachGesture {
            awaitPointerEventScope {
                val down = awaitFirstDown()
                val result = layoutResult() ?: return@awaitPointerEventScope
                val tagRange = findLinkRangeNearPosition(result, linkAnnotations, down)
                if (tagRange != null) {
                    down.consumeDownChange()
                    val minimumTouchTargetSize = MinLinkTouchTargetSize.toSize()
                    val up = waitForUpOrCancellation(result, tagRange, minimumTouchTargetSize)
                    if (up != null) {
                        up.consumeDownChange()
                        onItemClick(tagRange.tag, tagRange.item)
                    }
                }
            }
        }
    }
    return this
        .then(drawTappableBounds)
        .then(clicks)
}

/**
 * Finds the link nearest to the clicked position, or null if there isn't a link nearby. A link is considered nearby
 * if it is within the minimum touch size range.
 */
private fun PointerInputScope.findLinkRangeNearPosition(
    layoutResult: TextLayoutResult,
    linkAnnotations: List<AnnotatedString.Range<String>>,
    down: PointerInputChange,
): AnnotatedString.Range<String>? {
    var minDistance = Float.MAX_VALUE
    var closestCharOffset = -1
    var result: AnnotatedString.Range<String>? = null
    // Loop through all the characters in all of the links to find the character that's closest to where the down
    // event occurred.
    for (annotation in linkAnnotations) {
        for (c in annotation.start until annotation.end) {
            val bounds = layoutResult.getBoundingBox(c)
            if (bounds.width <= 0) continue

            val distance = (down.position - bounds.center).getDistanceSquared()
            if (distance < minDistance) {
                minDistance = distance
                closestCharOffset = c
                result = annotation
            }
        }
    }
    if (result != null) {
        val extendedTouchPadding = calculateExtendedTouchPaddingRequiredForCharacterInLink(
            layoutResult,
            closestCharOffset,
            result,
            MinLinkTouchTargetSize.toSize()
        )
        if (down.isOutOfCharacterBounds(layoutResult, closestCharOffset, extendedTouchPadding)) {
            return null
        }
    }
    return result
}

/**
 * Copied from [androidx.compose.foundation.gestures.waitForUpOrCancellation] with some tweaks. Rather than cancelling
 * the gesture when the pointer leaves the composable bounds, we cancel the event when the pointer leaves the current
 * link bounds.
 */
private suspend fun AwaitPointerEventScope.waitForUpOrCancellation(
    layoutResult: TextLayoutResult,
    linkRange: AnnotatedString.Range<String>,
    minimumTouchTargetSize: Size,
): PointerInputChange? {
    while (true) {
        val event = awaitPointerEvent(PointerEventPass.Main)
        if (event.changes.fastAll { it.changedToUp() }) {
            // All pointers are up
            return event.changes[0]
        }
        if (event.changes.fastAny {
                it.consumed.downChange || it.isOutOfLinkBounds(
                    layoutResult,
                    linkRange,
                    minimumTouchTargetSize
                )
            }
        ) {
            return null // Canceled
        }

        val consumeCheck = awaitPointerEvent(PointerEventPass.Final)
        if (consumeCheck.changes.fastAny { it.positionChangeConsumed() }) {
            return null
        }
    }
}

/**
 * Checks to see if any character in this link range is out of bounds of the current pointer event.
 */
private fun PointerInputChange.isOutOfLinkBounds(
    layoutResult: TextLayoutResult,
    linkRange: AnnotatedString.Range<String>,
    minimumTouchTargetSize: Size,
): Boolean {
    for (offset in linkRange.start until linkRange.end) {
        val extendedTouchPadding = calculateExtendedTouchPaddingRequiredForCharacterInLink(
            layoutResult,
            offset,
            linkRange,
            minimumTouchTargetSize
        )
        if (!isOutOfCharacterBounds(layoutResult, offset, extendedTouchPadding)) {
            return false
        }
    }
    return true
}

private fun PointerInputChange.isOutOfCharacterBounds(
    layoutResult: TextLayoutResult,
    offset: Int,
    extendedTouchPadding: Size,
): Boolean {
    val bounds = if (type != PointerType.Touch) {
        layoutResult.getBoundingBox(offset)
    } else {
        layoutResult.getBoundingBox(offset).inflate(extendedTouchPadding)
    }
    val x = position.x
    val y = position.y
    return x < bounds.left || x > bounds.right || y < bounds.top || y > bounds.bottom
}

/**
 * Calculates how much additional padding is required in order to make the link's bounds at least as large as
 * [minimumTouchTargetSize].
 *
 */
private fun calculateExtendedTouchPaddingRequiredForCharacterInLink(
    layoutResult: TextLayoutResult,
    offset: Int,
    range: AnnotatedString.Range<String>,
    minimumTouchTargetSize: Size,
): Size {
    val line = layoutResult.getLineForOffset(offset)
    val lineHeight = layoutResult.getLineBottom(line) - layoutResult.getLineTop(line)
    var linkWidthOnLine = 0f
    for (c in range.start until range.end) {
        if (layoutResult.getLineForOffset(c) == line) {
            val bounds = layoutResult.getBoundingBox(c)
            // Strangely, newlines can have negative width (presumably because the lhs of a newline character is at
            // the end of a line, but the rhs of the newline character is at the beginning of the following line). We
            // can safely ignore newlines here though!
            if (bounds.width > 0) {
                linkWidthOnLine += bounds.width
            }
        }
    }
    return Size(
        (minimumTouchTargetSize.width - linkWidthOnLine).coerceAtLeast(0f) / 2f,
        (minimumTouchTargetSize.height - lineHeight).coerceAtLeast(0f) / 2f
    )
}

private fun Rect.inflate(size: Size): Rect {
    return Rect(
        left = left - size.width,
        top = top - size.height,
        right = right + size.width,
        bottom = bottom + size.height
    )
}

private fun Modifier.drawDebugBounds(
    linkAnnotations: List<AnnotatedString.Range<String>>,
    layoutResult: () -> TextLayoutResult?,
) = then(Modifier.drawBehind {
    val result = layoutResult() ?: return@drawBehind
    for (annotation in linkAnnotations) {
        for (i in annotation.start until annotation.end) {
            val extendedTouchPadding = calculateExtendedTouchPaddingRequiredForCharacterInLink(
                result,
                i,
                annotation,
                MinLinkTouchTargetSize.toSize()
            )
            val bounds = result.getBoundingBox(i).inflate(extendedTouchPadding)
            // Strangely, newlines can have negative width (presumably because the lhs of a newline character
            // is at the end of a line, but the rhs of the newline character is at the beginning of the
            // following line). We can safely ignore newlines here though!
            if (bounds.width > 0) {
                drawRect(
                    Color.Magenta,
                    Offset(bounds.left, bounds.top),
                    Size(ceil(bounds.width), ceil(bounds.height))
                )
            }
        }
    }
})

private val MinLinkTouchTargetSize = DpSize(32.dp, 32.dp)
private const val ShowDebugBounds = false

@OptIn(ExperimentalTextApi::class)
inline fun <R : Any> AnnotatedString.Builder.withUrl(
    url: String,
    color: Color,
    crossinline block: AnnotatedString.Builder.() -> R,
): R {
    return withAnnotation(RichTextTag.URL, url) {
        withStyle(SpanStyle(color = color)) {
            block(this)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
inline fun <R : Any> AnnotatedString.Builder.withProfileMention(
    pubkey: String,
    color: Color,
    crossinline block: AnnotatedString.Builder.() -> R,
): R {
    return withAnnotation(RichTextTag.PROFILE, pubkey) {
        withStyle(SpanStyle(color = color)) {
            block(this)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
inline fun <R : Any> AnnotatedString.Builder.withNoteMention(
    id: String,
    color: Color,
    crossinline block: AnnotatedString.Builder.() -> R,
): R {
    return withAnnotation(RichTextTag.NOTE, id) {
        withStyle(SpanStyle(color = color)) {
            block(this)
        }
    }
}

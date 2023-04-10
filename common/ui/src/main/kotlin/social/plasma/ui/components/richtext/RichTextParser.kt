package social.plasma.ui.components.richtext

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import social.plasma.models.Mention
import social.plasma.models.NoteMention
import social.plasma.models.ProfileMention

class RichTextParser constructor(
    private val linkColor: Color,
) {
    private val urlRegex = Regex("(https?://\\S+)")
    private val mentionRegex = Regex("#\\[\\d+]")

    /**
     * Builds an annotated string using the note's mentions.
     */
    fun parse(
        input: String,
        mentions: Map<Int, Mention>,
    ): AnnotatedString {
        return buildAnnotatedString {
            val lines = input.split("\n")

            lines.forEachIndexed { lineIndex, line ->
                val words = line.split(" ")

                words.forEachIndexed { wordIndex, word ->

                    when {
                        urlRegex.matches(word) -> appendUrl(word)
                        mentionRegex.containsMatchIn(word) -> appendMention(word, mentions)
                        else -> append(word)
                    }

                    if (wordIndex != words.lastIndex) {
                        append(" ")
                    }
                }

                if (lineIndex != lines.lastIndex) {
                    append("\n")
                }
            }
        }
    }

    private fun AnnotatedString.Builder.appendUrl(word: String) {
        withUrl(word, linkColor) {
            append(word)
        }
    }

    private fun AnnotatedString.Builder.appendMention(
        word: String,
        mentions: Map<Int, Mention>,
    ) {
        var prevRange = IntRange(0, 0)
        for (match in mentionRegex.findAll(word)) {
            // prepend text between matches in the same word
            val leadingText = word.substring(prevRange.last, match.range.first)
            append(leadingText)

            prevRange = match.range
            val placeholder = match.value
            val key = placeholder.substring(2, placeholder.length - 1).toInt()
            mentions[key]?.let { mention ->
                when (mention) {
                    is ProfileMention -> withProfileMention(
                        pubkey = mention.pubkey.key.hex(),
                        color = linkColor,
                    ) {
                        append(mention.text)
                    }

                    is NoteMention -> withNoteMention(
                        id = mention.noteId.hex,
                        color = linkColor,
                    ) {
                        append(mention.text)
                    }
                }
            } ?: run {
                append(placeholder)
            }
        }

        // Append text after the last match in a word
        val trailingText = word.substring(prevRange.last + 1, word.length)
        append(trailingText)
    }
}

object RichTextTag {
    const val URL = "URL"
    const val PROFILE = "PROFILE"
    const val NOTE = "NOTE"
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

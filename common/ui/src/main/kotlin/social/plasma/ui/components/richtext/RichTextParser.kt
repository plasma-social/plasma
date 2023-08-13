package social.plasma.ui.components.richtext

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import app.cash.nostrino.crypto.PubKey
import shortBech32
import social.plasma.models.Mention
import social.plasma.models.NoteMention
import social.plasma.models.ProfileMention
import timber.log.Timber

class RichTextParser constructor(
    private val linkColor: Color,
) {
    private val urlRegex = Regex("(https?://\\S+)")
    private val hashtagRegex = Regex("#\\w+")
    private val mentionRegex = Regex("#\\[\\d+]")
    private val npubRegex = Regex("(nostr:npub)[\\da-z]{1,83}")

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
                        hashtagRegex.matches(word) -> appendHashtag(word)
                        mentionRegex.containsMatchIn(word) -> appendMention(word, mentions)
                        npubRegex.containsMatchIn(word) -> appendNpub(word, mentions)
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


    private fun AnnotatedString.Builder.appendNpub(word: String, mentions: Map<Int, Mention>) {
        var prevRange = IntRange(0, 0)

        for (match in npubRegex.findAll(word)) {
            val leadingText = word.substring(prevRange.last, match.range.first)
            append(leadingText)
            prevRange = IntRange(
                match.range.first,
                match.range.last + 1 // increase by 1 to include the last char
            )

            val pubkey = try {
                PubKey.parse(match.value.removePrefix("nostr:"))
            } catch (e: Throwable) {
                Timber.w(e, "Attempted to parse invalid pubkey: %s", match.value)
                null
            }

            if (pubkey == null) {
                append(match.value)
            } else {
                val mention =
                    mentions.values.firstOrNull { (it as? ProfileMention)?.pubkey == pubkey }

                withProfileMention(
                    pubkey = pubkey.hex(),
                    color = linkColor,
                ) {
                    append(mention?.text ?: pubkey.shortBech32())
                }
            }
        }

        val trailingText = word.substring(prevRange.last, word.length)
        append(trailingText)
    }

    private fun AnnotatedString.Builder.appendHashtag(word: String) {
        withHashTag(word, linkColor) {
            append(word)
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
    const val HASHTAG = "HASHTAG"
}

@OptIn(ExperimentalTextApi::class)
inline fun <R : Any> AnnotatedString.Builder.withProfileMention(
    pubkey: String,
    color: Color,
    crossinline block: AnnotatedString.Builder.() -> R,
): R {
    return withAnnotation(RichTextTag.PROFILE, pubkey) {
        withStyle(SpanStyle(color = color, fontWeight = FontWeight.SemiBold)) {
            block(this)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
inline fun <R : Any> AnnotatedString.Builder.withHashTag(
    tag: String,
    color: Color,
    crossinline block: AnnotatedString.Builder.() -> R,
): R {
    return withAnnotation(RichTextTag.HASHTAG, tag) {
        withStyle(SpanStyle(color = color, fontWeight = FontWeight.SemiBold)) {
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
        withStyle(SpanStyle(color = color, fontWeight = FontWeight.SemiBold)) {
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
        withStyle(SpanStyle(color = color, fontWeight = FontWeight.SemiBold)) {
            block(this)
        }
    }
}

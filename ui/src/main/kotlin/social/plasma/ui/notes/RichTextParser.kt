package social.plasma.ui.notes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import social.plasma.ui.components.richtext.Mention
import social.plasma.ui.components.richtext.NoteMention
import social.plasma.ui.components.richtext.ProfileMention
import social.plasma.ui.components.richtext.withNoteMention
import social.plasma.ui.components.richtext.withProfileMention
import social.plasma.ui.components.richtext.withUrl

class RichTextParser constructor(
    private val linkColor: Color,
) {
    private val urlRegex = Regex("(https?://\\S+)")
    private val mentionRegex = Regex("#\\[[0-9]+]")

    /**
     * Builds an annotated string using the note's mentions.
     */
    suspend fun parse(
        input: String,
        mentions: Map<Int, Mention>,
    ): AnnotatedString = withContext(Dispatchers.Default) {
        buildAnnotatedString {
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
                        pubkey = mention.pubkey.hex,
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

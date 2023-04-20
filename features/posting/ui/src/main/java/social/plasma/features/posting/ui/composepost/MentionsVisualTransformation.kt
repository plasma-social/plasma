package social.plasma.features.posting.ui.composepost

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import social.plasma.models.ProfileMention
import social.plasma.ui.components.richtext.withHashTag
import social.plasma.ui.components.richtext.withProfileMention

class MentionsVisualTransformation constructor(
    private val highlightColor: Color,
    private val mentions: Map<String, ProfileMention>,
) : VisualTransformation {
    private val bech32Regex = Regex("(@npub)[\\da-z]{1,83}")
    private val hashTagRegex = Regex("(#\\w+)")

    override fun filter(text: AnnotatedString): TransformedText {
        val (transformedText, offsetMapping) = replaceMentions(text)

        return TransformedText(
            text = transformedText,
            offsetMapping = offsetMapping,
        )
    }

    private fun replaceMentions(text: AnnotatedString): Pair<AnnotatedString, MentionOffsetMapping> {
        val originalToTransformedOffsets = mutableListOf<Int>()
        val transformedToOriginalOffsets = mutableListOf<Int>()

        val transformedText = buildAnnotatedString {
            var originalOffset = 0
            var transformedOffset = 0

            val lines = text.split("\n")

            for ((lineIndex, line) in lines.withIndex()) {
                val words = line.split(" ")

                for ((index, word) in words.withIndex()) {
                    val transformedWord = getTransformedWord(word)

                    originalToTransformedOffsets.addAll(List(word.length) { transformedOffset + it })
                    transformedToOriginalOffsets.addAll(List(transformedWord.length) { originalOffset + it })
                    originalOffset += word.length
                    transformedOffset += transformedWord.length

                    if (index != words.lastIndex) {
                        append(" ")
                    }
                }

                if (lineIndex != lines.lastIndex) {
                    append("\n")
                }
            }
        }

        return transformedText to MentionOffsetMapping(
            text.text,
            transformedText.text,
            originalToTransformedOffsets,
            transformedToOriginalOffsets
        )
    }

    private fun AnnotatedString.Builder.getTransformedWord(word: String): String {
        var transformedWord = ""

        when {
            bech32Regex.containsMatchIn(word) -> {
                val match = bech32Regex.find(word)
                val mention = mentions[match?.value]

                if (match != null && mention != null) {
                    transformedWord = prependTextBeforeMention(match, word)

                    // TODO replace .key.hex() with .hex() everywhere when nostrino is updated
                    withProfileMention(mention.pubkey.key.hex(), highlightColor) {
                        append(mention.text)
                        transformedWord += mention.text
                    }

                    transformedWord = appendTextAfterMention(word, match, transformedWord)
                }
            }

            hashTagRegex.containsMatchIn(word) -> {
                val match = hashTagRegex.find(word)

                if (match != null) {
                    transformedWord = prependTextBeforeMention(match, word)

                    withHashTag(match.value, highlightColor) {
                        append(match.value)
                        transformedWord += match.value
                    }

                    transformedWord = appendTextAfterMention(word, match, transformedWord)
                }
            }

            else -> {
                append(word)
                transformedWord = word
            }
        }


        return transformedWord
    }

    private fun AnnotatedString.Builder.appendTextAfterMention(
        word: String,
        match: MatchResult,
        transformedWord: String,
    ): String {
        return if (word.length > match.value.length) {
            val textAfter = word.substring(match.range.last + 1, word.length)
            append(textAfter)
            transformedWord + textAfter
        } else {
            transformedWord
        }
    }

    private fun AnnotatedString.Builder.prependTextBeforeMention(
        match: MatchResult,
        word: String,
    ): String {
        return if (match.range.first == 0) {
            ""
        } else {
            val textBefore = word.substring(0, match.range.first)
            append(textBefore)
            textBefore
        }
    }
}

class MentionOffsetMapping(
    private val originalText: String,
    private val transformedText: String,
    private val originalToTransformedOffsets: List<Int>,
    private val transformedToOriginalOffsets: List<Int>,
) : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        return originalToTransformedOffsets.getOrElse(offset) { transformedText.length }
    }

    override fun transformedToOriginal(offset: Int): Int {
        return transformedToOriginalOffsets.getOrElse(offset) { originalText.length }
    }
}


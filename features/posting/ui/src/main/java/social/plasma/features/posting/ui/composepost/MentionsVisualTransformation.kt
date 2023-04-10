package social.plasma.features.posting.ui.composepost

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import social.plasma.models.ProfileMention
import social.plasma.ui.components.richtext.withProfileMention

class MentionsVisualTransformation constructor(
    private val highlightColor: Color,
    private val mentions: Map<String, ProfileMention>,
) : VisualTransformation {
    private val bech32Regex = Regex("(@npub)[\\da-z]{1,83}")

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
                    val match = bech32Regex.find(word)
                    val mention = mentions[match?.value]
                    var transformedWord = ""

                    if (match != null && mention != null) {
                        if (match.range.first != 0) {
                            val textBefore = word.substring(0, match.range.first)
                            append(textBefore)
                            transformedWord += textBefore
                        }

                        // TODO replace .key.hex() with .hex() everywhere when nostrino is updated
                        withProfileMention(mention.pubkey.key.hex(), highlightColor) {
                            append(mention.text)
                            transformedWord += mention.text
                        }

                        if (word.length > match.value.length) {
                            val textAfter = word.substring(match.range.last + 1, word.length)
                            append(textAfter)
                            transformedWord += textAfter
                        }
                    } else {
                        append(word)
                        transformedWord = word
                    }

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


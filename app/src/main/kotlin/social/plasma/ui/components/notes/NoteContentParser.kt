package social.plasma.ui.components.notes

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import social.plasma.ui.components.richtext.Mention
import social.plasma.ui.components.richtext.NoteMention
import social.plasma.ui.components.richtext.ProfileMention
import social.plasma.ui.components.richtext.withNoteMention
import social.plasma.ui.components.richtext.withProfileMention
import social.plasma.ui.components.richtext.withUrl
import social.plasma.ui.theme.DarkThemeColors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteContentParser @Inject constructor() {
    //TODO make color dynamic
    private val linkColor = DarkThemeColors.textLink
    private val imageUrlRegex = Regex("https?:/(/[^/]+)+\\.(?:jpg|gif|png|jpeg|svg|webp)")
    private val urlRegex = Regex("(https?://\\S+)")
    private val mediaUrlRegex = Regex("https?:/(/[^/]+)+\\.(?:mp4|mov|webm|mp3)")
    private val mentionRegex = Regex("#\\[[0-9]+]")

    fun parseNote(note: String, mentions: Map<Int, Mention>): List<NoteUiModel.ContentBlock> {
        // TODO parse these as part of the text parsing, so we can do a single pass on the note content
        val imageUrls = note.parseImageUrls()
        val videoUrls = note.parseVideoUrls()

        val imageContent = when {
            imageUrls.size == 1 -> NoteUiModel.ContentBlock.Image(imageUrls.first())
            imageUrls.size > 1 -> NoteUiModel.ContentBlock.Carousel(imageUrls.toList())
            else -> null
        }

        val videoBlocks = videoUrls.map {
            NoteUiModel.ContentBlock.Video(videoUrl = it)
        }

        val noteTextContent = note.replace(imageUrlRegex, "").replace(mediaUrlRegex, "")

        val urlPreviewBlocks = noteTextContent.parseUrlPreviews().map {
            NoteUiModel.ContentBlock.UrlPreview(it)
        }

        val contentBlocks = listOf(
            NoteUiModel.ContentBlock.Text(
                buildTextContent(
                    noteTextContent, mentions
                )
            )
        ) + urlPreviewBlocks + imageContent + videoBlocks

        return contentBlocks.filterNotNull()
    }

    /**
     * Builds an annotated string using the note's mentions.
     */
    private fun buildTextContent(
        input: String,
        mentions: Map<Int, Mention>,
    ): AnnotatedString = buildAnnotatedString {
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
                // TODO should we just drop mentions if there isn't a match?
                append(placeholder)
            }
        }

        // Append text after the last match in a word
        val trailingText = word.substring(prevRange.last + 1, word.length)
        append(trailingText)
    }

    private fun String.parseImageUrls(): Set<String> =
        imageUrlRegex.findAll(this).map { it.value }.toSet()

    private fun String.parseVideoUrls(): Set<String> =
        mediaUrlRegex.findAll(this).map { it.value }.toSet()

    private fun String.parseUrlPreviews(): Set<String> =
        urlRegex.findAll(this).map { it.value }.toSet()
}

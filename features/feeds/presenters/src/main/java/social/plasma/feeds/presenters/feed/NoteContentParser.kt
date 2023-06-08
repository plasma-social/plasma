package social.plasma.feeds.presenters.feed

import social.plasma.features.feeds.screens.feed.ContentBlock
import social.plasma.models.Mention
import social.plasma.models.NoteId
import social.plasma.models.crypto.Bech32
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteContentParser @Inject constructor() {
    private val urlRegex = Regex("(https?://\\S+)")
    private val imageUrlRegex = Regex("https?:/(/[^/]+)+\\.(?:jpg|gif|png|jpeg|svg|webp)")
    private val mediaUrlRegex = Regex("https?:/(/[^/]+)+\\.(?:mp4|mov|webm|mp3)")
    private val noteQuoteRegex = Regex("(nostr:note)[\\da-z]{1,83}")


    fun parseNote(note: String, mentions: Map<Int, Mention>): List<ContentBlock> {
        // TODO parse these as part of the text parsing, so we can do a single pass on the note content
        val imageUrls = note.parseImageUrls()
        val videoUrls = note.parseVideoUrls()

        val imageContent = when {
            imageUrls.size == 1 -> ContentBlock.Image(imageUrls.first())
            imageUrls.size > 1 -> ContentBlock.Carousel(imageUrls.toList())
            else -> null
        }

        val videoBlocks = videoUrls.map {
            ContentBlock.Video(videoUrl = it)
        }

        val noteQuoteBlocks = note.parseNoteQuotes().map {
            ContentBlock.NoteQuote(NoteId.of(Bech32.decodeBytes(it.removePrefix("nostr:")).second))
        }

        val noteTextContent =
            note.replace(imageUrlRegex, "").replace(mediaUrlRegex, "").replace(noteQuoteRegex, "")

        val urlPreviewBlocks = noteTextContent.parseUrlPreviews().map {
            ContentBlock.UrlPreview(it)
        }

        val contentBlocks = listOf(
            ContentBlock.Text(
                noteTextContent,
                mentions,
            )
        ) + noteQuoteBlocks + urlPreviewBlocks + imageContent + videoBlocks

        return contentBlocks.filterNotNull()
    }

    private fun String.parseImageUrls(): Set<String> =
        imageUrlRegex.findAll(this).map { it.value }.toSet()

    private fun String.parseVideoUrls(): Set<String> =
        mediaUrlRegex.findAll(this).map { it.value }.toSet()

    private fun String.parseUrlPreviews(): Set<String> =
        urlRegex.findAll(this).map { it.value }.toSet()

    private fun String.parseNoteQuotes(): Set<String> =
        noteQuoteRegex.findAll(this).map { it.value }.toSet()
}

package social.plasma.domain.interactors

import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import social.plasma.domain.InvokeStatus
import social.plasma.domain.InvokeSuccess
import social.plasma.domain.ResultInteractor
import social.plasma.models.EventTag
import social.plasma.models.HashTag
import social.plasma.models.NoteId
import social.plasma.models.NoteWithUser
import social.plasma.models.PubKeyTag
import social.plasma.models.Tag
import social.plasma.models.crypto.Bech32
import social.plasma.shared.repositories.api.NoteRepository
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class SendNote @Inject constructor(
    private val noteRepository: NoteRepository,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : ResultInteractor<SendNote.Params, InvokeStatus>() {
    private val bech32Regex = Regex("(@npub|@note|npub|note)[\\da-z]{1,83}")
    private val hashTagRegex = Regex("#\\w+")

    data class Params(
        val content: String,
        val parentNote: NoteWithUser? = null,
    )

    override suspend fun doWork(params: Params): InvokeStatus {
        val additionalTags = mutableSetOf<Tag>().apply {
            if (params.parentNote != null) {
                val noteEntity = params.parentNote.noteEntity
                noteEntity.tags.forEachIndexed { index, tag ->
                    if (tag.firstOrNull() == "p" && tag.size >= 2) {
                        add(PubKeyTag(PubKey(tag[1].decodeHex())))
                    }
                    if (tag.firstOrNull() == "e" && tag.size >= 2 && index == 0) {
                        add(EventTag(NoteId(tag[1])))
                    }
                }

                add(EventTag(NoteId(noteEntity.id)))
                add(PubKeyTag(PubKey(noteEntity.pubkey.decodeHex())))
            }

            addAll(getContentHashTags(params.content))
        }

        return withContext(ioDispatcher) {
            val (content, tags) = replaceContentWithPlaceholders(params.content, additionalTags)
            noteRepository.sendNote(content, tags.toList())
            InvokeSuccess
        }
    }

    private fun getContentHashTags(content: String): Set<Tag> = hashTagRegex.findAll(content)
        .map { HashTag.parse(it.value) }
        .toSet()

    private fun replaceContentWithPlaceholders(
        content: String,
        additionalTags: Set<Tag> = emptySet(),
    ): Pair<String, Set<Tag>> {
        // TODO Make it efficient 🤷🏽‍
        val additionalPTags = additionalTags.filterIsInstance(PubKeyTag::class.java).toSet()
        val additionalETags = additionalTags.filterIsInstance(EventTag::class.java).toSet()

        val npubMentions = extractBech32Mentions(content, type = "npub")
        val noteMentions = extractBech32Mentions(content, type = "note")

        val allPTags =
            additionalPTags + npubMentions.map { PubKeyTag(PubKey(it.second.toByteString())) }
        val allETags = additionalETags + noteMentions.map { EventTag(NoteId.of(it.second)) }

        var contentWithPlaceholders =
            npubMentions.fold(content) { acc, (originalContent, hex) ->
                val placeholderIndex =
                    allETags.count() + allPTags.indexOfFirst { it.pubKey == PubKey(hex.toByteString()) }
                acc.replace(originalContent, "#[$placeholderIndex]")
            }

        contentWithPlaceholders =
            noteMentions.fold(contentWithPlaceholders) { acc, (originalContent, hex) ->
                val placeholderIndex = allETags.indexOfFirst { it.noteId == NoteId.of(hex) }
                acc.replace(originalContent, "#[$placeholderIndex]")
            }

        val tags = allETags + allPTags + additionalTags

        return Pair(contentWithPlaceholders, tags)
    }


    private fun extractBech32Mentions(content: String, type: String): Set<Pair<String, ByteArray>> {
        return bech32Regex.findAll(content).map { matchResult ->
            val matchValue = with(matchResult.value) {
                if (this.startsWith("@"))
                    this.drop(1)
                else
                    this
            }

            val (hrp, bytes) = try {
                Bech32.decodeBytes(matchValue)
            } catch (e: Exception) {
                Triple(null, null, null)
            }

            val hex = when (hrp) {
                type -> bytes!!
                else -> null
            }

            hex?.let { Pair(matchResult.value, it) }
        }.filterNotNull().toSet()
    }

}

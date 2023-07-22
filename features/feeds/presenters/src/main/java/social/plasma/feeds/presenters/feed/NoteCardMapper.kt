package social.plasma.feeds.presenters.feed

import app.cash.nostrino.crypto.PubKey
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.firstOrNull
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import shortBech32
import social.plasma.domain.interactors.GetNip5Status
import social.plasma.features.feeds.presenters.R
import social.plasma.features.feeds.screens.feed.ContentBlock
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.models.Event
import social.plasma.models.Mention
import social.plasma.models.NoteId
import social.plasma.models.NoteMention
import social.plasma.models.NoteWithUser
import social.plasma.models.ProfileMention
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.repositories.api.NoteRepository
import social.plasma.shared.repositories.api.UserMetadataRepository
import social.plasma.shared.utils.api.InstantFormatter
import social.plasma.shared.utils.api.StringManager
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

class NoteCardMapper @Inject constructor(
    private val noteContentParser: NoteContentParser,
    private val userMetaDataRepository: UserMetadataRepository,
    private val accountStateRepository: AccountStateRepository,
    private val noteRepository: NoteRepository,
    private val moshi: Moshi,
    private val instantFormatter: InstantFormatter,
    private val getNip5Status: GetNip5Status,
    private val stringManager: StringManager,
) {

    suspend fun toFeedItem(noteWithUser: NoteWithUser): FeedItem {
        return when (noteWithUser.noteEntity.kind) {
            Event.Kind.Repost -> createRepostUiModel(noteWithUser)
            else -> createNoteUiModel(noteWithUser)
        }
    }

    private suspend fun createNoteUiModel(noteWithUser: NoteWithUser): FeedItem {
        val note = noteWithUser.noteEntity
        val author = noteWithUser.userMetadataEntity
        val authorPubKey = PubKey(note.pubkey.decodeHex())
        val shortBech32 by lazy { authorPubKey.shortBech32() }

        return FeedItem.NoteCard(
            id = note.id,
            key = note.id,
            name = author?.name ?: shortBech32,
            displayName = author?.userFacingName ?: shortBech32,
            avatarUrl = author?.picture,
            nip5Identifier = author?.nip05,
            content = noteContentParser.parseNote(note.content, note.tags.toIndexedMap()),
            cardLabel = buildBannerLabel(note.tags),
            timePosted = Instant.ofEpochSecond(note.createdAt).relativeTime(),
            replyCount = "",
            shareCount = "",
            likeCount = note.reactionCount,
            userPubkey = authorPubKey,
            isLiked = isLiked(note.id),
            isNip5Valid = { pubKey, identifier ->
                getNip5Status.executeSync(
                    GetNip5Status.Params(
                        pubKey = pubKey,
                        identifier = identifier
                    )
                ).isValid()
            },
            nip5Domain = author?.nip05?.split("@")?.getOrNull(1),
            zapsEnabled = author?.tipAddress != null,
            tipAddress = author?.tipAddress,
        )
    }

    // TODO consolidate with the regular notes mapping
    private suspend fun createRepostUiModel(
        noteWithUser: NoteWithUser,
    ): FeedItem {
        val note = noteWithUser.noteEntity
        val repostedNote = try {
            moshi.adapter(Event::class.java).fromJson(note.content)
        } catch (e: Exception) {
            Timber.w("Unable to parse reposted note content: %s", note.content)
            null
        }

        // The paging library doesn't allow mapping to nullables, so instead we'll return a "hidden" note
        repostedNote ?: return FeedItem.NoteCard(
            id = note.id,
            userPubkey = PubKey(note.pubkey.decodeHex()),
            hidden = true,
        )

        val authorPubKey = PubKey(repostedNote.pubKey)
        val author = userMetaDataRepository.observeUserMetaData(authorPubKey).firstOrNull()

        return FeedItem.NoteCard(
            id = repostedNote.id.hex(),
            key = note.id,
            name = author?.name ?: authorPubKey.shortBech32(),
            displayName = author?.userFacingName ?: authorPubKey.shortBech32(),
            avatarUrl = author?.picture,
            nip5Identifier = author?.nip05,
            headerContent = ContentBlock.Text(
                "Boosted by #[0]",
                mentions = mapOf(
                    0 to ProfileMention(
                        text = noteWithUser.userMetadataEntity?.name
                            ?: PubKey(note.pubkey.decodeHex()).shortBech32(),
                        pubkey = PubKey(note.pubkey.decodeHex())
                    )
                )
            ),
            content = noteContentParser.parseNote(
                repostedNote.content,
                repostedNote.tags.toIndexedMap()
            ),
            cardLabel = buildBannerLabel(repostedNote.tags),
            timePosted = Instant.ofEpochSecond(repostedNote.createdAt.epochSecond)
                .relativeTime(),
            replyCount = "",
            shareCount = "",
            likeCount = 0,
            userPubkey = authorPubKey,
            isLiked = isLiked(note.id),
            isNip5Valid = { pubKey, identifier ->
                getNip5Status.executeSync(
                    GetNip5Status.Params(
                        pubKey = pubKey,
                        identifier = identifier
                    )
                ).isValid()
            },
            nip5Domain = author?.nip05?.split("@")?.getOrNull(1),
            zapsEnabled = author?.tipAddress != null,
            tipAddress = author?.tipAddress,
        )
    }

    private suspend fun isLiked(id: String): Boolean {
        val myPubkey = accountStateRepository.getPublicKey()
        return noteRepository.isNoteLiked(PubKey(myPubkey?.toByteString()!!), NoteId(id))
    }

    private suspend fun buildBannerLabel(tags: List<List<String>>): String? {
        val pTags = tags.filter { it.firstOrNull() == "p" && it.size >= 2 }

        val referencedNames = getReferencedNames(pTags).toList()

        return when {
            referencedNames.isEmpty() -> null
            referencedNames.size == 1 -> stringManager.getFormattedString(
                R.string.replying_to_single,
                mapOf("user" to referencedNames[0])
            )

            else -> {
                val additionalReferencedNames = pTags.size - 2
                stringManager.getFormattedString(
                    R.string.replying_to_many, mapOf(
                        "additionalUserCount" to additionalReferencedNames,
                        "firstUser" to referencedNames[0],
                        "secondUser" to referencedNames[1],
                    )
                )
            }
        }
    }

    private suspend fun getReferencedNames(tags: List<List<String>>): MutableSet<String> {
        val referencedNames = mutableSetOf<String>()

        val MAX_P_TAGS_TO_DISPLAY = 2

        tags.take(MAX_P_TAGS_TO_DISPLAY).forEach { tag ->
            val pubkey = PubKey(tag[1].decodeHex())

            val userMetaData = userMetaDataRepository.observeUserMetaData(pubkey).firstOrNull()
            val userName = (userMetaData?.userFacingName ?: pubkey.shortBech32())

            referencedNames.add(userName)
        }
        return referencedNames
    }

    private suspend fun List<List<String>>.toIndexedMap(): Map<Int, Mention> =
        mapIndexed { index, tag ->
            when (tag.firstOrNull()) {
                "p" -> {
                    val pubkey = PubKey(tag[1].decodeHex())

                    val userName =
                        (userMetaDataRepository.observeUserMetaData(pubkey).firstOrNull()?.name
                            ?: pubkey.shortBech32())

                    return@mapIndexed index to ProfileMention(
                        pubkey = pubkey,
                        text = "@$userName"
                    )
                }

                "e" -> {
                    val noteId = NoteId(tag[1])
                    val mentionText = try {
                        "@${noteId.shortBech32}"
                    } catch (e: Exception) {
                        Timber.e(e)
                        null
                    }

                    mentionText ?: return@mapIndexed null
                    return@mapIndexed index to NoteMention(
                        noteId = noteId,
                        text = mentionText
                    )
                }

                else -> null
            }
        }.filterNotNull().toMap()

    private fun Instant.relativeTime(): String {
        return instantFormatter.getRelativeTime(this)

    }
}

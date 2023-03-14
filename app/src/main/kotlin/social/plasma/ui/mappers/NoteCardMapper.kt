package social.plasma.ui.mappers

import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.firstOrNull
import social.plasma.db.notes.NoteWithUser
import social.plasma.db.reactions.ReactionDao
import social.plasma.di.KeyType
import social.plasma.di.UserKey
import social.plasma.models.NoteId
import social.plasma.models.PubKey
import social.plasma.nostr.models.Event
import social.plasma.prefs.Preference
import social.plasma.repository.UserMetaDataRepository
import social.plasma.ui.components.richtext.Mention
import social.plasma.ui.components.richtext.NoteMention
import social.plasma.ui.components.richtext.ProfileMention
import social.plasma.ui.notes.NoteContentParser
import social.plasma.ui.notes.NoteUiModel
import social.plasma.ui.util.InstantFormatter
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

class NoteCardMapper @Inject constructor(
    private val userMetaDataRepository: UserMetaDataRepository,
    @UserKey(KeyType.Public) private val myPubkeyPref: Preference<ByteArray>,
    private val reactionDao: ReactionDao,
    private val noteContentParser: NoteContentParser,
    private val moshi: Moshi,
    private val instantFormatter: InstantFormatter,
) {

    suspend fun toNoteUiModel(noteWithUser: NoteWithUser): NoteUiModel {
        return when (noteWithUser.noteEntity.kind) {
            Event.Kind.Repost -> createRepostUiModel(noteWithUser)
            else -> createNoteUiModel(noteWithUser)
        }
    }

    private suspend fun createNoteUiModel(noteWithUser: NoteWithUser): NoteUiModel {
        val note = noteWithUser.noteEntity
        val author = noteWithUser.userMetadataEntity
        val authorPubKey = PubKey(note.pubkey)

        return NoteUiModel(
            key = note.id,
            id = note.id,
            name = author?.name ?: authorPubKey.shortBech32,
            content = noteContentParser.parseNote(note.content, note.tags.toIndexedMap()),
            avatarUrl = author?.picture,
            timePosted = Instant.ofEpochSecond(note.createdAt).relativeTime(),
            replyCount = "",
            shareCount = "",
            likeCount = note.reactionCount,
            userPubkey = authorPubKey,
            nip5Identifier = author?.nip05,
            nip5Domain = author?.nip05?.split("@")?.getOrNull(1),
            displayName = author?.displayName?.takeIf { it.isNotBlank() } ?: author?.name
            ?: authorPubKey.shortBech32,
            cardLabel = buildBannerLabel(note.tags),
            isLiked = isLiked(note.id),
            isNip5Valid = userMetaDataRepository::isNip5Valid
        )
    }

    private suspend fun createRepostUiModel(
        noteWithUser: NoteWithUser,
    ): NoteUiModel {
        val note = noteWithUser.noteEntity
        val repostedNote = try {
            moshi.adapter(Event::class.java).fromJson(note.content)
        } catch (e: Exception) {
            Timber.w("Unable to parse reposted note content: %s", note.content)
            null
        }

        // The paging library doesn't allow mapping to nullables, so instead we'll return a "hidden" note
        repostedNote ?: return NoteUiModel(
            id = note.id,
            hidden = true,
            userPubkey = PubKey(note.pubkey)
        )

        val authorPubKey = PubKey(repostedNote.pubKey.hex())
        val author = userMetaDataRepository.getById(repostedNote.pubKey.hex())

        return NoteUiModel(
            key = note.id,
            id = repostedNote.id.hex(),
            name = author?.name ?: authorPubKey.shortBech32,
            content = noteContentParser.parseNote(
                repostedNote.content,
                repostedNote.tags.toIndexedMap()
            ),
            avatarUrl = author?.picture,
            timePosted = Instant.ofEpochSecond(repostedNote.createdAt.epochSecond)
                .relativeTime(),
            replyCount = "",
            shareCount = "",
            likeCount = 0,
            userPubkey = authorPubKey,
            nip5Identifier = author?.nip05,
            nip5Domain = author?.nip05?.split("@")?.getOrNull(1),
            displayName = author?.displayName?.takeIf { it.isNotBlank() } ?: author?.name
            ?: authorPubKey.shortBech32,
            headerContent = NoteUiModel.ContentBlock.Text(
                "Boosted by #[0]",
                mentions = mapOf(
                    0 to ProfileMention(
                        text = noteWithUser.userMetadataEntity?.name
                            ?: PubKey(note.pubkey).shortBech32,
                        pubkey = PubKey(note.pubkey)
                    )
                )
            ),
            cardLabel = buildBannerLabel(repostedNote.tags),
            isLiked = isLiked(repostedNote.id.hex()),
            isNip5Valid = userMetaDataRepository::isNip5Valid
        )
    }

    private suspend fun isLiked(id: String): Boolean {
        val myPubkey = myPubkeyPref.get(null)
        return reactionDao.isNoteLiked(PubKey.of(myPubkey!!).hex, id)
    }

    private suspend fun buildBannerLabel(tags: List<List<String>>): String? {
        val referencedNames = getReferencedNames(tags)

        return if (referencedNames.isNotEmpty()) {
            referencedNames.generateBannerLabel()
        } else null
    }

    private suspend fun getReferencedNames(tags: List<List<String>>): MutableSet<String> {
        val referencedNames = mutableSetOf<String>()

        tags.forEachIndexed { index, it ->
            if (it.firstOrNull() == "p") {
                val pubkey = PubKey(it[1])

                val userName =
                    (userMetaDataRepository.observeUserMetaData(pubkey.hex).firstOrNull()?.name
                        ?: pubkey.shortBech32)

                referencedNames.add(userName)
            }
        }
        return referencedNames
    }

    private suspend fun List<List<String>>.toIndexedMap(): Map<Int, Mention> =
        mapIndexed { index, tag ->
            when (tag.firstOrNull()) {
                "p" -> {
                    val pubkey = PubKey(tag[1])

                    val userName =
                        (userMetaDataRepository.observeUserMetaData(pubkey.hex).firstOrNull()?.name
                            ?: pubkey.shortBech32)

                    return@mapIndexed index to ProfileMention(pubkey = pubkey, text = "@$userName")
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
                    return@mapIndexed index to NoteMention(noteId = noteId, text = mentionText)
                }

                else -> null
            }
        }.filterNotNull().toMap()

    private fun Instant.relativeTime(): String {
        return instantFormatter.getRelativeTime(this)

    }


    private fun Iterable<String>.generateBannerLabel(): String {
        return "Replying to ${this.joinToString()}"
    }
}

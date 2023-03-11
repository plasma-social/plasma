package social.plasma.ui.mappers

import android.text.format.DateUtils
import kotlinx.coroutines.flow.firstOrNull
import social.plasma.db.notes.NoteView
import social.plasma.db.notes.NoteWithUser
import social.plasma.db.reactions.ReactionDao
import social.plasma.db.usermetadata.UserMetadataDao
import social.plasma.models.NoteId
import social.plasma.models.PubKey
import social.plasma.repository.AccountStateRepository
import social.plasma.repository.UserMetaDataRepository
import social.plasma.ui.components.richtext.Mention
import social.plasma.ui.components.richtext.NoteMention
import social.plasma.ui.components.richtext.ProfileMention
import social.plasma.ui.notes.NoteContentParser
import social.plasma.ui.notes.NoteUiModel
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteCardMapper @Inject constructor(
    private val userMetadataDao: UserMetadataDao,
    private val userMetaDataRepository: UserMetaDataRepository,
    private val accountStateRepository: AccountStateRepository,
    private val reactionDao: ReactionDao,
    private val noteContentParser: NoteContentParser,
) {

    suspend fun toNoteUiModel(noteWithUser: NoteWithUser): NoteUiModel {
        val note = noteWithUser.noteEntity
        val author = noteWithUser.userMetadataEntity
        val authorPubKey = PubKey(note.pubkey)

        return NoteUiModel(
            id = note.id,
            name = author?.name ?: authorPubKey.shortBech32,
            content = noteContentParser.parseNote(note.content, note.tags.toIndexedMap()),
            avatarUrl = author?.picture
                ?: "https://api.dicebear.com/5.x/bottts/jpg?seed=${authorPubKey.hex}",
            timePosted = Instant.ofEpochSecond(note.createdAt).relativeTime(),
            replyCount = "",
            shareCount = "",
            likeCount = note.reactionCount,
            userPubkey = authorPubKey,
            nip5Identifier = author?.nip05,
            nip5Domain = author?.nip05?.split("@")?.getOrNull(1),
            displayName = author?.displayName?.takeIf { it.isNotBlank() } ?: author?.name
            ?: authorPubKey.shortBech32,
            cardLabel = note.buildBannerLabel(),
            isLiked = note.isLiked(),
            isNip5Valid = userMetaDataRepository::isNip5Valid
        )
    }

    private suspend fun NoteView.isLiked(): Boolean {
        val myPubkey = accountStateRepository.getPublicKey()!!

        // TODO what if the post is liked but we haven't fetched the reaction from relays yet?
        return reactionDao.isNoteLiked(PubKey.of(myPubkey).hex, id)
    }

    private suspend fun NoteView.buildBannerLabel(): String? {
        val referencedNames = getReferencedNames(this)

        return if (referencedNames.isNotEmpty()) {
            referencedNames.generateBannerLabel()
        } else null
    }

    private suspend fun getReferencedNames(note: NoteView): MutableSet<String> {
        val referencedNames = mutableSetOf<String>()

        note.tags.forEachIndexed { index, it ->
            if (it.firstOrNull() == "p") {
                val pubkey = PubKey(it[1])

                val userName = (userMetadataDao.getById(pubkey.hex).firstOrNull()?.name
                    ?: pubkey.shortBech32)

                if (pubkey.hex != note.pubkey) {
                    referencedNames.add(userName)
                }
            }
        }
        return referencedNames
    }

    private suspend fun List<List<String>>.toIndexedMap(): Map<Int, Mention> =
        mapIndexed { index, tag ->
            when (tag.firstOrNull()) {
                "p" -> {
                    val pubkey = PubKey(tag[1])

                    val userName = (userMetadataDao.getById(pubkey.hex).firstOrNull()?.name
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
        return DateUtils.getRelativeTimeSpanString(
            this.toEpochMilli(),
            Instant.now().toEpochMilli(),
            DateUtils.SECOND_IN_MILLIS,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_ABBREV_ALL
        ).toString()
    }


    private fun Iterable<String>.generateBannerLabel(): String {
        return "Replying to ${this.joinToString()}"
    }
}

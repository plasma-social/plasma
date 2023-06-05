package social.plasma.shared.repositories.real

import androidx.paging.PagingSource
import app.cash.nostrino.crypto.PubKey
import app.cash.nostrino.crypto.SecKey
import kotlinx.coroutines.flow.Flow
import okio.ByteString.Companion.toByteString
import social.plasma.data.daos.NotesDao
import social.plasma.models.EventTag
import social.plasma.models.HashTag
import social.plasma.models.NoteId
import social.plasma.models.NoteWithUser
import social.plasma.models.PubKeyTag
import social.plasma.models.Tag
import social.plasma.nostr.relay.RelayManager
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.repositories.api.NoteRepository
import java.time.Instant
import javax.inject.Inject

internal class RealNoteRepository @Inject constructor(
    private val relay: RelayManager,
    private val accountStateRepository: AccountStateRepository,
    private val notesDao: NotesDao,
) : NoteRepository {
    override suspend fun getById(noteId: NoteId): NoteWithUser? {
        return notesDao.getById(noteId.hex)
    }

    override suspend fun sendNote(content: String, tags: List<Tag>) {
        val nostrTags = tags.map { tag ->
            when (tag) {
                is EventTag -> listOf("e", tag.noteId.hex)
                is PubKeyTag -> listOf("p", tag.pubKey.key.hex())
                is HashTag -> listOf("t", tag.name)
            }
        }.toSet()

        val secKey = SecKey(
            accountStateRepository.getSecretKey()?.toByteString()
                ?: throw IllegalStateException("Secret key required to send notes")
        )

        relay.sendNote(
            content,
            tags = nostrTags,
            secKey = secKey
        )
    }

    override fun observePagedContactsNotes(): PagingSource<Int, NoteWithUser> {
        val pubkey = PubKey(accountStateRepository.getPublicKey()?.toByteString()!!)

        return notesDao.observePagedContactsNotes(pubkey.key.hex())
    }

    override fun observePagedNotifications(): PagingSource<Int, NoteWithUser> {
        val pubkey = PubKey(accountStateRepository.getPublicKey()?.toByteString()!!)

        return notesDao.observePagedNotifications(pubkey.key.hex())
    }

    override fun observePagedContactsReplies(): PagingSource<Int, NoteWithUser> {
        val pubkey = PubKey(accountStateRepository.getPublicKey()?.toByteString()!!)

        return notesDao.observePagedContactsReplies(pubkey.key.hex())
    }

    override fun observePagedUserNotes(pubKey: PubKey): PagingSource<Int, NoteWithUser> {
        return notesDao.observePagedUserNotes(pubKey.key.hex())
    }

    override fun observePagedThreadNotes(noteId: NoteId): PagingSource<Int, NoteWithUser> {
        return notesDao.observePagedThreadNotes(noteId.hex)
    }

    override suspend fun refreshContactsNotes(): List<NoteWithUser> {
        // TODO
        return emptyList()
    }

    override suspend fun isNoteLiked(byPubKey: PubKey, noteId: NoteId): Boolean {
        return notesDao.isNoteLiked(byPubKey.key.hex(), noteId.hex)
    }

    override fun observePagedNotesWithContent(hashtag: HashTag): PagingSource<Int, NoteWithUser> {
        return notesDao.observePagedNotesWithContent("%${hashtag.displayName}%")
    }

    override fun observePagedHashTagNotes(hashtag: HashTag): PagingSource<Int, NoteWithUser> {
        return notesDao.observePagedNotesWithHashtag(hashtag.name)
    }

    override fun observeHashTagNoteCount(hashtag: HashTag, since: Instant?): Flow<Long> {
        return notesDao.observeHashTagNoteCount(hashtag.name, since?.epochSecond ?: 0)
    }
}

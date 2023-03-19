package social.plasma.shared.repositories.real

import androidx.paging.PagingSource
import okio.ByteString.Companion.toByteString
import social.plasma.data.daos.NotesDao
import social.plasma.models.EventTag
import social.plasma.models.NoteId
import social.plasma.models.NoteWithUser
import social.plasma.models.PubKey
import social.plasma.models.PubKeyTag
import social.plasma.models.Tag
import social.plasma.models.crypto.KeyPair
import social.plasma.nostr.relay.Relay
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.repositories.api.NoteRepository
import javax.inject.Inject

internal class RealNoteRepository @Inject constructor(
    private val relay: Relay,
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
                is PubKeyTag -> listOf("p", tag.pubKey.hex)
            }
        }.toSet()

        val pubkey = accountStateRepository.getPublicKey() ?: throw IllegalStateException("Public key required to send notes")
        val secretKey = accountStateRepository.getSecretKey() ?: throw IllegalStateException("Secret key required to send notes")

        relay.sendNote(
            content,
            tags = nostrTags,
            keyPair = KeyPair(pubkey.toByteString(), secretKey.toByteString())
        )
    }

    override fun observePagedContactsNotes(): PagingSource<Int, NoteWithUser> {
        val pubkey = PubKey.of(accountStateRepository.getPublicKey()!!)

        return notesDao.observePagedContactsNotes(pubkey.hex)
    }

    override fun observePagedNotifications(): PagingSource<Int, NoteWithUser> {
        val pubkey = PubKey.of(accountStateRepository.getPublicKey()!!)

        return notesDao.observePagedNotifications(pubkey.hex)
    }

    override fun observePagedContactsReplies(): PagingSource<Int, NoteWithUser> {
        val pubkey = PubKey.of(accountStateRepository.getPublicKey()!!)

       return notesDao.observePagedContactsReplies(pubkey.hex)
    }

    override fun observePagedUserNotes(pubKey: PubKey): PagingSource<Int, NoteWithUser> {
        return notesDao.observePagedUserNotes(pubKey.hex)
    }

    override fun observePagedThreadNotes(noteId: NoteId): PagingSource<Int, NoteWithUser> {
        return notesDao.observePagedThreadNotes(noteId.hex)
    }

    override suspend fun refreshContactsNotes(): List<NoteWithUser> {
        // TODO
        return emptyList()
    }

    override suspend fun isNoteLiked(byPubKey: PubKey, noteId: NoteId): Boolean {
        return notesDao.isNoteLiked(byPubKey.hex, noteId.hex)
    }
}
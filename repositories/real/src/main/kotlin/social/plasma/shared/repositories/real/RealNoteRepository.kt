package social.plasma.shared.repositories.real

import androidx.paging.PagingSource
import app.cash.nostrino.crypto.PubKey
import app.cash.nostrino.crypto.SecKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.toByteString
import social.plasma.data.daos.EventsDao
import social.plasma.data.daos.NotesDao
import social.plasma.models.Event
import social.plasma.models.EventTag
import social.plasma.models.HashTag
import social.plasma.models.NoteId
import social.plasma.models.NoteWithUser
import social.plasma.models.PubKeyTag
import social.plasma.models.Tag
import social.plasma.models.events.EventEntity
import social.plasma.nostr.relay.RelayManager
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.Filter
import social.plasma.nostr.relay.message.RelayMessage
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.repositories.api.NoteRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

internal class RealNoteRepository @Inject constructor(
    private val relay: RelayManager,
    private val accountStateRepository: AccountStateRepository,
    private val notesDao: NotesDao,
    private val eventsDao: EventsDao,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : NoteRepository {
    override suspend fun getById(noteId: NoteId): NoteWithUser? {
        return notesDao.getById(noteId.hex)
    }

    override fun observeById(noteId: NoteId): Flow<NoteWithUser?> {
        return notesDao.observeById(noteId.hex).onEach {
            if (it == null) {
                refreshNoteById(noteId)
            }
        }
    }

    override fun observeEventById(noteId: NoteId): Flow<EventEntity?> {
        return notesDao.observeEventById(noteId.hex).onEach {
            if (it == null) {
                refreshNoteById(noteId)
            }
        }
    }

    private suspend fun refreshNoteById(noteId: NoteId) {
        val unsubscribeMessage =
            relay.subscribe(ClientMessage.SubscribeMessage(Filter(ids = setOf(noteId.hex))))

        relay.relayMessages.filterIsInstance<RelayMessage.EventRelayMessage>()
            .filter { it.subscriptionId == unsubscribeMessage.subscriptionId }
            .onEach { relayMessage ->
                eventsDao.insert(listOf(relayMessage.event.toEventEntity()))
            }.onCompletion { relay.unsubscribe(unsubscribeMessage) }.flowOn(ioDispatcher).first()
    }

    private fun Event.toEventEntity() = EventEntity(
        id = id.hex(),
        pubkey = pubKey.hex(),
        createdAt = createdAt.epochSecond,
        kind = kind,
        tags = tags,
        content = content,
        sig = sig.hex(),
    )

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
            content, tags = nostrTags, secKey = secKey
        )
    }

    override fun observePagedContactsEvents(): PagingSource<Int, EventEntity> {
        val pubkey = PubKey(accountStateRepository.getPublicKey()?.toByteString()!!)

        return notesDao.observePagedContactsEvents(pubkey.key.hex())
    }

    override fun observeLikeCount(noteId: NoteId): Flow<Long> {
        return notesDao.observeLikeCount(noteId.hex)
    }

    override fun observePagedNotifications(): PagingSource<Int, EventEntity> {
        val pubkey = PubKey(accountStateRepository.getPublicKey()?.toByteString()!!)

        return notesDao.observePagedNotifications(pubkey.key.hex())
    }

    override fun observePagedContactsReplies(): PagingSource<Int, EventEntity> {
        val pubkey = PubKey(accountStateRepository.getPublicKey()?.toByteString()!!)

        return notesDao.observePagedContactsReplies(pubkey.key.hex())
    }

    override fun observePagedUserNotes(pubKey: PubKey): PagingSource<Int, EventEntity> {
        return notesDao.observePagedUserNotes(pubKey.key.hex())
    }

    override fun observePagedThreadNotes(noteId: NoteId): PagingSource<Int, EventEntity> {
        return notesDao.observePagedThreadNotes(noteId.hex)
    }

    override suspend fun refreshContactsNotes(): List<NoteWithUser> {
        // TODO
        return emptyList()
    }

    override fun isNoteLiked(noteId: NoteId): Flow<Boolean> = flow {
        val pubKey = getPublicKeySuspended()
        emitAll(notesDao.isNoteLiked(pubKey, noteId.hex))
    }

    private suspend fun getPublicKeySuspended(): String = withContext(ioDispatcher) {
        PubKey(accountStateRepository.getPublicKey()!!.toByteString()).hex()
    }

    override fun observePagedHashTagNotes(hashtag: HashTag): PagingSource<Int, EventEntity> {
        return notesDao.observePagedNotesWithHashtag(hashtag.name)
    }

    override fun observeHashTagNoteCount(hashtag: HashTag, since: Instant?): Flow<Long> {
        return notesDao.observeHashTagNoteCount(hashtag.name, since?.epochSecond ?: 0)
    }
}

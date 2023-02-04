package social.plasma.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import okio.ByteString.Companion.toByteString
import social.plasma.PubKey
import social.plasma.db.notes.*
import social.plasma.di.KeyType
import social.plasma.di.UserKey
import social.plasma.nostr.models.Event
import social.plasma.nostr.models.Note
import social.plasma.nostr.models.TypedEvent
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage.EventMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.EventRefiner
import social.plasma.nostr.relay.message.Filter
import social.plasma.prefs.Preference
import social.plasma.utils.chunked
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

interface NoteRepository {
    fun observeGlobalNotes(): Flow<PagingData<NoteWithUser>>

    fun observeProfileNotes(pubkey: String): Flow<PagingData<NoteWithUser>>

    fun observeContactsNotes(): Flow<PagingData<NoteWithUser>>

    fun observeContactsNotesAndReplies(): Flow<PagingData<NoteWithUser>>

    suspend fun refreshContactsNotes(): List<NoteEntity>

    fun observeMentions(): Flow<PagingData<NoteWithUser>>

    suspend fun postNote(content: String)
}

class RealNoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val contactListRepository: ContactListRepository,
    @UserKey(KeyType.Public) private val myPubKey: Preference<ByteArray>,
    @UserKey(KeyType.Secret) private val mySecretKey: Preference<ByteArray>,
    private val eventRefiner: EventRefiner,
    private val relay: Relay,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : NoteRepository {

    override fun observeGlobalNotes(): Flow<PagingData<NoteWithUser>> {
        return merge(
            Pager(
                config = PagingConfig(pageSize = 25, maxSize = 500),
                pagingSourceFactory = { noteDao.globalNotesPagingSource() }
            ).flow,

            fetchWithNoteDbSync(
                SubscribeMessage(Filter.globalFeedNotes),
                NoteSource.Global
            )

        ).filterIsInstance()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeProfileNotes(pubkey: String): Flow<PagingData<NoteWithUser>> {
        return merge(
            Pager(
                config = PagingConfig(pageSize = 25, maxSize = 500),
                pagingSourceFactory = { noteDao.userNotesAndRepliesPagingSource(listOf(pubkey)) }
            ).flow,

            noteDao.getLatestNoteEpoch(pubkey).take(1)
                .map { epoch -> epoch?.let { Instant.ofEpochMilli(it) } ?: Instant.EPOCH }
                .flatMapLatest { since ->
                    fetchWithNoteDbSync(
                        subscribeMessage = SubscribeMessage(Filter.userNotes(pubkey, since)),
                        source = NoteSource.Profile
                    )
                }
                .flowOn(ioDispatcher),
        ).filterIsInstance()
    }


    override fun observeContactsNotes(): Flow<PagingData<NoteWithUser>> {
        val myPubkey = PubKey.of(myPubKey.get(null)!!).hex

        return contactListRepository.observeContactLists(myPubkey)
            .filter { it.isNotEmpty() }.distinctUntilChanged().flatMapLatest {
                val contactNpubList = it.map { it.pubKey.hex() }

                merge(
                    Pager(
                        config = PagingConfig(pageSize = 25, maxSize = 500),
                        pagingSourceFactory = { noteDao.notesBySource(NoteSource.Contacts) }
                    ).flow,

                    fetchWithNoteDbSync(
                        SubscribeMessage(Filter.userNotes(contactNpubList.toSet())),
                        NoteSource.Contacts
                    )
                ).filterIsInstance()
            }
    }

    override fun observeContactsNotesAndReplies(): Flow<PagingData<NoteWithUser>> {
        val myPubkey = PubKey.of(myPubKey.get(null)!!).hex

        return contactListRepository.observeContactLists(myPubkey)
            .filter { it.isNotEmpty() }.distinctUntilChanged().flatMapLatest {
                val contactNpubList = it.map { it.pubKey.hex() }

                merge(
                    Pager(
                        config = PagingConfig(pageSize = 25, maxSize = 500),
                        pagingSourceFactory = {
                            noteDao.notesAndRepliesBySource(NoteSource.Contacts)
                        }
                    ).flow,

                    fetchWithNoteDbSync(
                        SubscribeMessage(Filter.userNotes(contactNpubList.toSet())),
                        NoteSource.Contacts
                    )
                ).filterIsInstance()
            }
    }

    override suspend fun refreshContactsNotes(): List<NoteEntity> {
        val myPubkey = PubKey.of(myPubKey.get(null)!!).hex

        val latestRefresh = noteDao.getLatestNoteEpoch(myPubkey, NoteSource.Contacts)
            .firstOrNull()
        val since = latestRefresh?.let { Instant.ofEpochMilli(it) } ?: Instant.EPOCH
        val contacts = contactListRepository.syncContactList(pubkey = myPubkey).first()
        val contactNpubList = contacts.map { it.pubKey.hex() }
        return fetchWithNoteDbSync(
            SubscribeMessage(
                Filter.userNotes(
                    pubKeys = contactNpubList.toSet(),
                    since = since,
                )
            ),
            NoteSource.Contacts
        ).first()
    }

    override fun observeMentions(): Flow<PagingData<NoteWithUser>> {
        val myPubkey = PubKey.of(myPubKey.get(null)!!).hex

        return merge(
            Pager(
                config = PagingConfig(pageSize = 25, maxSize = 500),
                pagingSourceFactory = {
                    noteDao.notesAndRepliesBySource(NoteSource.Notifications)
                }
            ).flow,

            fetchWithNoteDbSync(
                SubscribeMessage(Filter(pTags = setOf(myPubkey), limit = 500)),
                NoteSource.Notifications
            )
        ).filterIsInstance()
    }

    override suspend fun postNote(content: String) {
        val myPubkey = myPubKey.get(null)!!
        val mySecretKey = mySecretKey.get(null)!!
        val event = Event.createEvent(
            pubKey = myPubkey.toByteString(),
            secretKey = mySecretKey.toByteString(),
            createdAt = Instant.now(),
            kind = Event.Kind.Note,
            tags = emptyList(),
            content = content,
        )
        relay.send(EventMessage(event = event))
    }

    private fun fetchWithNoteDbSync(subscribeMessage: SubscribeMessage, source: NoteSource) =
        relay.subscribe(subscribeMessage)
            .map { eventRefiner.toNote(it) }
            .filterNotNull()
            .onEach { note ->
                val sourceNoteId = note.id.hex()

                val noteReferences = note.tags.filter { it.firstOrNull() == "e" }.map {
                    NoteReferenceEntity(sourceNoteId, targetNote = it[1])
                }

                noteDao.insertNoteReference(noteReferences)
            }
            .map { it.toNoteEntity(source) }
            .chunked(500, 200)
            .onEach { noteDao.insert(it) }
            .flowOn(ioDispatcher)

    companion object {
        const val CHUNK_MAX_SIZE = 500
        const val CHUNK_DELAY = 1000L
    }
}

fun TypedEvent<Note>.toNoteEntity(
    source: NoteSource,
): NoteEntity = NoteEntity(
    id = id.hex(),
    pubkey = pubKey.hex(),
    createdAt = createdAt.toEpochMilli(),
    content = content.text,
    sig = sig.hex(),
    source = source,
    // TODO This isn't fool-proof.
    //  What happens with notes that mention other notes but aren't replies?
    isReply = tags.any { it.firstOrNull() == "e" },
    tags = tags,
)

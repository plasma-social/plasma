package social.plasma.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import social.plasma.PubKey
import social.plasma.db.notes.NoteDao
import social.plasma.db.notes.NoteEntity
import social.plasma.db.notes.NoteSource
import social.plasma.db.notes.NoteWithUser
import social.plasma.db.reactions.ReactionDao
import social.plasma.db.reactions.ReactionEntity
import social.plasma.di.KeyType
import social.plasma.di.UserKey
import social.plasma.nostr.models.Note
import social.plasma.nostr.models.Reaction
import social.plasma.nostr.models.TypedEvent
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.EventRefiner
import social.plasma.nostr.relay.message.Filters
import social.plasma.nostr.relay.message.SubscribeMessage
import social.plasma.prefs.Preference
import social.plasma.utils.chunked
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

interface NoteRepository {
    fun observeGlobalNotes(): Flow<PagingData<NoteWithUser>>
    fun observeProfileNotes(pubkey: String): Flow<PagingData<NoteWithUser>>
    fun observeNoteReactionCount(id: String): Flow<Unit>
    fun observeContactsNotesPaging(): Flow<PagingData<NoteWithUser>>

    suspend fun refreshContactsNotes(): List<NoteEntity>
}

class RealNoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val reactionDao: ReactionDao,
    private val contactListRepository: ContactListRepository,
    @UserKey(KeyType.Public) private val myPubKey: Preference<ByteArray>,
    private val eventRefiner: EventRefiner,
    private val relay: Relay,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : NoteRepository {

    override fun observeGlobalNotes(): Flow<PagingData<NoteWithUser>> {
        return merge(
            Pager(
                config = PagingConfig(pageSize = 25, maxSize = 500),
                pagingSourceFactory = { noteDao.allNotesWithUsersPagingSource() }
            ).flow,

            fetchWithNoteDbSync(
                SubscribeMessage(filters = Filters.globalFeedNotes),
                NoteSource.Global
            )

        ).filterIsInstance()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeProfileNotes(pubkey: String): Flow<PagingData<NoteWithUser>> {
        return merge(
            Pager(
                config = PagingConfig(pageSize = 25, maxSize = 500),
                pagingSourceFactory = { noteDao.userNotesPagingSource(listOf(pubkey)) }
            ).flow,

            noteDao.getLatestNoteEpoch(pubkey).take(1)
                .map { epoch -> epoch?.let { Instant.ofEpochMilli(it) } ?: Instant.EPOCH }
                .flatMapLatest { since ->
                    fetchWithNoteDbSync(
                        SubscribeMessage(
                            filters = Filters.userNotes(
                                pubkey,
                                since
                            )
                        ), NoteSource.Profile
                    )
                }
                .flowOn(ioDispatcher),
        ).filterIsInstance()
    }


    override fun observeContactsNotesPaging(): Flow<PagingData<NoteWithUser>> {
        val myPubkey = PubKey.of(myPubKey.get(null)!!).hex

        return contactListRepository.observeContactLists(myPubkey)
            .filter { it.isNotEmpty() }.distinctUntilChanged().flatMapLatest {
                val contactNpubList = it.map { it.pubKey.hex() }

                merge(
                    Pager(
                        config = PagingConfig(pageSize = 25, maxSize = 500),
                        pagingSourceFactory = { noteDao.userNotesPagingSource(contactNpubList) }
                    ).flow,

                    fetchWithNoteDbSync(
                        SubscribeMessage(filters = Filters.userNotes(contactNpubList.toSet())),
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
                filters = Filters.userNotes(
                    pubKeys = contactNpubList.toSet(),
                    since = since,
                )
            ),
            NoteSource.Contacts
        ).first()
    }

    override fun observeNoteReactionCount(id: String): Flow<Unit> {
        return relay.subscribe(SubscribeMessage(filters = Filters.noteReactions(id)))
            .map { eventRefiner.toReaction(it) }
            .map { it?.toReactionEntity() }
            .filterNotNull()
            .chunked(CHUNK_MAX_SIZE, CHUNK_DELAY)
            .onEach { reactionDao.insert(it) }
            .flowOn(ioDispatcher)
            .map { }
    }

    private fun fetchWithNoteDbSync(subscribeMessage: SubscribeMessage, source: NoteSource) =
        relay.subscribe(subscribeMessage)
            .map { eventRefiner.toNote(it) }
            .filterNotNull()
            .map { it.toNoteEntity(source) }
            .chunked(500, 200)
            .onEach { noteDao.insert(it) }
            .flowOn(ioDispatcher)

    companion object {
        const val CHUNK_MAX_SIZE = 500
        const val CHUNK_DELAY = 1000L
    }
}

private fun TypedEvent<Reaction>.toReactionEntity(): ReactionEntity? {
    val noteId = noteIdFromTags()
    noteId ?: return null

    return ReactionEntity(
        id = id.hex(),
        content = content.text,
        createdAt = createdAt.toEpochMilli(),
        noteId = noteId
    )
}

private fun TypedEvent<Reaction>.noteIdFromTags(): String? {
    // TODO we need to figure out what to do with the rest of the tags :/
    val noteId = this.tags.lastOrNull { it.firstOrNull() == "e" }?.getOrNull(1)
    return noteId
}

private fun TypedEvent<Note>.toNoteEntity(
    source: NoteSource,
): NoteEntity = NoteEntity(
    id = id.hex(),
    pubkey = pubKey.hex(),
    createdAt = createdAt.toEpochMilli(),
    content = content.text,
    sig = sig.hex(),
    source = source,
)

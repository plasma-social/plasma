package social.plasma.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import social.plasma.db.notes.NoteDao
import social.plasma.db.notes.NoteEntity
import social.plasma.db.notes.NoteSource
import social.plasma.db.notes.NoteWithUser
import social.plasma.db.reactions.ReactionDao
import social.plasma.db.reactions.ReactionEntity
import social.plasma.models.Note
import social.plasma.models.Reaction
import social.plasma.models.TypedEvent
import social.plasma.relay.Relay
import social.plasma.relay.message.EventRefiner
import social.plasma.relay.message.Filters
import social.plasma.relay.message.SubscribeMessage
import social.plasma.utils.chunked
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

interface NoteRepository {
    fun observeGlobalNotes(): Flow<PagingData<NoteWithUser>>
    fun observeProfileNotes(pubkey: String): Flow<PagingData<NoteWithUser>>
    fun observeNoteReactionCount(id: String): Flow<Unit>
}

class RealNoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val reactionDao: ReactionDao,
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

            relay.subscribe(SubscribeMessage(filters = Filters.globalFeedNotes))
                .map { eventRefiner.toNote(it) }
                .filterNotNull()
                .map { it.toNoteEntity(NoteSource.Global) }
                .chunked(500, 200)
                .onEach { noteDao.insert(it) }
                .flowOn(ioDispatcher)
        ).filterIsInstance()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeProfileNotes(pubkey: String): Flow<PagingData<NoteWithUser>> {
        return merge(
            Pager(
                config = PagingConfig(pageSize = 25, maxSize = 500),
                pagingSourceFactory = { noteDao.userNotesPagingSource(pubkey) }
            ).flow,

            noteDao.getLatestNoteEpoch(pubkey).take(1)
                .map { epoch -> epoch?.let { Instant.ofEpochMilli(it) } ?: Instant.EPOCH }
                .flatMapLatest { since ->
                    observeUserNotes(pubkey, since).chunked(
                        CHUNK_MAX_SIZE,
                        checkIntervalMillis = CHUNK_DELAY
                    ).onEach {
                        noteDao.insert(it)
                    }
                }
                .flowOn(ioDispatcher),
        ).filterIsInstance()
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

    private fun observeUserNotes(
        pubkey: String,
        since: Instant,
    ): Flow<NoteEntity> {
        return relay.subscribe(
            SubscribeMessage(filters = Filters.userNotes(pubkey, since = since))
        ).map { eventRefiner.toNote(it) }
            .filterNotNull()
            .map { it.toNoteEntity(NoteSource.Profile) }
    }

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

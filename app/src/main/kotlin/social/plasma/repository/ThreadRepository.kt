package social.plasma.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import social.plasma.db.notes.NoteDao
import social.plasma.db.notes.NoteReferenceEntity
import social.plasma.db.notes.NoteSource
import social.plasma.db.notes.NoteWithUser
import social.plasma.db.notes.PubkeyReferenceEntity
import social.plasma.db.usermetadata.UserMetadataDao
import social.plasma.nostr.models.Event
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.EventRefiner
import social.plasma.nostr.relay.message.Filter
import social.plasma.utils.chunked
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.streams.toList

interface ThreadRepository {
    fun observeThreadNotes(noteId: String): Flow<List<NoteWithUser>>
}

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class RealThreadRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val relay: Relay,
    @Named("io") val ioDispatcher: CoroutineContext,
    private val eventRefiner: EventRefiner,
    private val userMetadataDao: UserMetadataDao,
) : ThreadRepository {

    override fun observeThreadNotes(noteId: String): Flow<List<NoteWithUser>> {
        return merge(
            requestThread(noteId),
            noteDao.observeThreadNotes(noteId)
                .distinctUntilChanged()
                .filterNotNull()
                .flatMapLatest { thread ->
                    val allNotes =
                        sortedSetOf(
                            comparator = { a, b -> a.createdAt.compareTo(b.createdAt) },
                            thread.note
                        ).apply {
                            addAll(thread.childrenNotes)
                            addAll(thread.parentNotes)
                        }

                    val noteFlows = allNotes.map { noteEntity ->
                        userMetadataDao.getById(noteEntity.pubkey)
                            .distinctUntilChanged()
                            .zip(flowOf(noteEntity)) { user, note ->
                                NoteWithUser(
                                    noteEntity = note!!,
                                    userMetadataEntity = user
                                )
                            }
                    }

                    combine(noteFlows) { it.asList() }
                }
        ).flowOn(ioDispatcher)
            .filterIsInstance()
    }

    private fun requestThread(noteId: String): Flow<Unit> = flow {
        val noteTags: List<List<String>> = noteDao.getById(noteId)?.noteEntity?.tags ?: run {
            relay.subscribe(SubscribeMessage(filter = Filter(ids = setOf(noteId))))
                .map { eventRefiner.toNote(it) }
                .filterNotNull()
                .take(1)
                .onEach {
                    noteDao.insert(it.toNoteEntity(source = NoteSource.Thread))
                }
                .map { it.tags }
                .first()
        }

        val parentNoteIds = noteTags.mapNotNull { if (it[0] == "e") it[1] else null }

        val childNotesFilter = Filter(eTags = setOf(noteId), kinds = setOf(Event.Kind.Note))

        val subscribeMessage = if (parentNoteIds.isNotEmpty()) {
            SubscribeMessage(
                Filter(ids = parentNoteIds.toSet()),
                childNotesFilter
            )
        } else {
            SubscribeMessage(childNotesFilter)
        }

        relay.subscribe(subscribeMessage)
            .map { eventRefiner.toNote(it) }
            .filterNotNull()
            .chunked(maxSize = 100, checkIntervalMillis = 100)
            .onEach { it ->
                val noteReferences = it.parallelStream().flatMap { note ->
                    val sourceNoteId = note.id.hex()

                    note.tags.parallelStream().filter { it.firstOrNull() == "e" }.map {
                        NoteReferenceEntity(sourceNoteId, targetNote = it[1])
                    }
                }

                val pubkeyReferences = it.parallelStream().flatMap { note ->
                    val sourceNoteId = note.id.hex()
                    
                    note.tags.parallelStream().filter { it.firstOrNull() == "p" }.map {
                        PubkeyReferenceEntity(sourceNoteId, pubkey = it[1])
                    }
                }

                noteDao.insertNoteReferences(noteReferences.toList())
                noteDao.insertPubkeyReferences(pubkeyReferences.toList())
            }
            .map { eventList -> eventList.map { it.toNoteEntity(NoteSource.Thread) } }
            .map { entityList ->
                noteDao.insert(entityList)
            }
            .collect(this)
    }
}

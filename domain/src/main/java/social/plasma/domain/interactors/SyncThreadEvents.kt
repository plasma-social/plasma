package social.plasma.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import social.plasma.data.daos.LastRequestDao
import social.plasma.data.daos.NotesDao
import social.plasma.domain.Interactor
import social.plasma.domain.SubjectInteractor
import social.plasma.models.LastRequestEntity
import social.plasma.models.NoteId
import social.plasma.models.Request
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.Filter
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class SyncThreadEvents @Inject constructor(
    private val relay: Relay,
    private val storeEvents: StoreEvents,
    private val notesDao: NotesDao,
    private val lastRequestDao: LastRequestDao,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : Interactor<SyncThreadEvents.Params>() {

    override suspend fun doWork(params: Params) = withContext(ioDispatcher) {
        val noteId = params.noteId

        val lastRequest =
            lastRequestDao.lastRequest(Request.SYNC_THREAD, noteId.hex)?.timestamp ?: Instant.EPOCH

        val noteTags: List<List<String>> = notesDao.getById(noteId.hex)?.noteEntity?.tags ?: run {
            relay.subscribe(SubscribeMessage(filter = Filter(ids = setOf(noteId.hex))))
                .take(1)
                .map { it.event.tags }
                .first()
        }

        val parentNoteIds = noteTags.mapNotNull { if (it[0] == "e" && it.size > 1) it[1] else null }

        val subscribeMessage = SubscribeMessage(
            Filter(ids = setOf(noteId.hex) + parentNoteIds),
            Filter(eTags = setOf(noteId.hex), since = lastRequest),
        )

        val subscription =
            relay.subscribe(subscribeMessage).distinctUntilChanged().map { it.event }.onEach {
                lastRequestDao.upsert(
                    LastRequestEntity(request = Request.SYNC_THREAD, resourceId = noteId.hex)
                )
            }

        storeEvents(subscription)
        storeEvents.flow.collect()
    }

    data class Params(
        val noteId: NoteId,
    )
}
package social.plasma.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import social.plasma.models.Note
import social.plasma.relay.Relay
import social.plasma.relay.Relays
import social.plasma.relay.Relays.Companion.relayUrlList
import social.plasma.relay.message.RelayMessage.EventRelayMessage
import java.util.*
import javax.inject.Inject

interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>
}

class RealNoteRepository @Inject constructor(
    private val relays: Relays,
) : NoteRepository {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val relayList: List<Relay> = relayUrlList.map { relays.relay(it) }

    private val relayFlows: List<Flow<List<EventRelayMessage>>> = relayList.map { relay ->
        relay.flowRelayMessages()
            // Quick and dirty way of caching previous emissions in memory
            .runningFold(emptyList()) { accumulator, value ->
                accumulator + value
            }
    }

    private val notesSharedFlow: SharedFlow<List<Note>> =
        combine(relayFlows) { values ->
            values.fold(TreeSet<Note> { l, r ->
                r.createdAt.compareTo(l.createdAt)
            }) { acc, list ->
                acc.addAll(list.mapNotNull { it.event.maybeToNote() })
                acc
            }.toList()
        }.shareIn(scope, SharingStarted.Eagerly, replay = 1)

    init {
        scope.launch {
            relayList.forEach { it.connectAndSubscribe() }
        }
    }

    override fun observeNotes(): Flow<List<Note>> {
        return notesSharedFlow
    }
}

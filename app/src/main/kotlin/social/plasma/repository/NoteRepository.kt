package social.plasma.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import social.plasma.models.Event
import social.plasma.models.Note
import social.plasma.relay.Relay
import social.plasma.relay.Relays
import social.plasma.relay.message.RelayMessage.EventRelayMessage
import javax.inject.Inject

interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>
}

class RealNoteRepository @Inject constructor(
    private val relays: Relays,
) : NoteRepository {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val relayUrlList = listOf(
        "wss://brb.io",
        "wss://relay.damus.io",
        "wss://relay.nostr.bg",
        "wss://nostr.oxtr.dev",
        "wss://nostr.v0l.io",
        "wss://nostr-pub.semisol.dev",
        "wss://relay.kronkltd.net",
        "wss://nostr.zebedee.cloud",
        "wss://no.str.cr",
        "wss://relay.nostr.info",
    )

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
            // TODO Find a more efficient way to keep this list flat and sorted
            values.asList()
                .flatten()
                .sortedByDescending { it.event.createdAt }
                .map { it.event.toNote() }
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

private fun Event.toNote() = Note(content = content, pubKey = pubKey.hex(), createdAt = createdAt)

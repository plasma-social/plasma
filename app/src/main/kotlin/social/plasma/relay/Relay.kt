package social.plasma.relay

import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.reactive.asFlow
import social.plasma.models.Note
import java.time.Instant

class Relay(private val service: RelayService) {

    fun flowNotes(): Flow<Note> = service.relayMessageFlow()
        .filter { it is RelayMessage.EventRelayMessage && it.event.kind == 1 }
        .map { it as RelayMessage.EventRelayMessage }
        .map { Note(it.event.content) }
        .asFlow()

    suspend fun connectAndSubscribe(
        filters: Filters = Filters(since = Instant.now().minusSeconds(600))
    ): Relay {
        flowWebSocketEvents()
            .filter { it is WebSocket.Event.OnConnectionOpened<*> }
            .take(1)
            .collect { subscribe(filters) }
        return this
    }

    private fun flowWebSocketEvents() = service.webSocketEventFlow().asFlow()

    private fun subscribe(filters: Filters) {
        service.sendSubscribe(RequestMessage(filters = filters))
    }
}
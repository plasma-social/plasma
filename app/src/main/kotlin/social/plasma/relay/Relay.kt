package social.plasma.relay

import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.reactive.asFlow
import social.plasma.relay.message.Filters
import social.plasma.relay.message.RelayMessage.EventRelayMessage
import social.plasma.relay.message.RequestMessage
import java.time.Instant

class Relay(private val service: RelayService) {

    fun flowRelayMessages(): Flow<EventRelayMessage> = service.relayMessageFlow()
        .asFlow()
        .filterIsInstance<EventRelayMessage>()

    suspend fun connectAndSubscribe(
        filters: Filters = Filters(since = Instant.now().minusSeconds(600))
    ): Relay {
        flowWebSocketEvents()
            .filterIsInstance<WebSocket.Event.OnConnectionOpened<*>>()
            .take(1)
            .collect { subscribe(filters) }
        return this
    }

    private fun flowWebSocketEvents() = service.webSocketEventFlow().asFlow()

    private fun subscribe(filters: Filters) {
        service.sendSubscribe(RequestMessage(filters = filters))
    }
}
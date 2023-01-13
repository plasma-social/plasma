package social.plasma.relay

import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.reactive.asFlow
import social.plasma.models.TypedEvent
import social.plasma.models.UserMetaData
import social.plasma.relay.message.EventRefiner
import social.plasma.relay.message.Filters
import social.plasma.relay.message.RelayMessage.EventRelayMessage
import social.plasma.relay.message.RequestMessage

class Relay(
    private val service: RelayService,
    private val eventRefiner: EventRefiner,
) {

    suspend fun connectAndSubscribe(
        filters: Filters = Filters.globalFeedNotes,
    ): Relay {
        service.webSocketEventFlow()
            .asFlow()
            .filterIsInstance<WebSocket.Event.OnConnectionOpened<*>>()
            .take(1)
            .collect { subscribe(filters) }
        return this
    }

    fun userMeta(pubKey: String): Flow<TypedEvent<UserMetaData>> {
        val requestMessage = RequestMessage(filters = Filters.userMetaData(pubKey))
        val flow = eventRefiner.toUserMetaData(flowRelayMessages())
            .take(1)
            .onCompletion {
                service.sendClose(requestMessage.toCloseMessage())
            }
        service.sendSubscribe(requestMessage)
        return flow
    }

    /** All events sent by the relay to the client */
    fun flowRelayMessages(): Flow<EventRelayMessage> = service.relayMessageFlow()
        .asFlow()
        .filterIsInstance()

    private fun subscribe(filters: Filters) {
        service.sendSubscribe(RequestMessage(filters = filters))
    }

}

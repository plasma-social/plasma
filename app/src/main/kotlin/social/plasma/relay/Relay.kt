package social.plasma.relay

import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.reactive.asFlow
import social.plasma.relay.message.Filters
import social.plasma.relay.message.RelayMessage.EventRelayMessage
import social.plasma.relay.message.RequestMessage

class Relay(
    private val service: RelayService,
) {

    // TODO break apart connectAndSubscribe such that all subscriptions are queued behind the
    //      successful connection
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

    fun subscribe(request: RequestMessage): Subscription {
        service.sendSubscribe(request)
        return Subscription(request.subscriptionId) {
            service.sendClose(request.toCloseMessage())
        }
    }

//    fun userMeta(pubKey: String): Flow<TypedEvent<UserMetaData>> {
//        val requestMessage = RequestMessage(filters = Filters.userMetaData(pubKey))
//        val flow = eventRefiner.toUserMetaData(flowRelayMessages())
//            .take(1)
//            .onCompletion {
//                service.sendClose(requestMessage.toCloseMessage())
//            }
//        service.sendSubscribe(requestMessage)
//        return flow
//    }

    /** All events sent by the relay to the client */
    fun flowRelayMessages(): Flow<EventRelayMessage> = service.relayMessageFlow()
        .asFlow()
        .filterIsInstance()

    private fun subscribe(filters: Filters) {
        service.sendSubscribe(RequestMessage(filters = filters))
    }

}

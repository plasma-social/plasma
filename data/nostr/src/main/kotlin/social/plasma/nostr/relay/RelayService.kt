package social.plasma.nostr.relay

import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.channels.ReceiveChannel
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.ClientMessage.EventMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.ClientMessage.UnsubscribeMessage
import social.plasma.nostr.relay.message.RelayMessage

interface RelayService {

    @Send
    fun sendSubscribe(msg: SubscribeMessage)

    @Send
    fun sendUnsubscribe(msg: UnsubscribeMessage)

    @Send
    fun sendEvent(msg: EventMessage)

    @Receive
    fun relayMessageChannel(): ReceiveChannel<RelayMessage>

    @Receive
    fun webSocketEventChannel(): ReceiveChannel<WebSocket.Event>

    @Send
    fun sendCountSubscribe(msg: ClientMessage.RequestCountMessage)
}

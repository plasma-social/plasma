package social.plasma.nostr.relay

import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Flowable
import social.plasma.nostr.relay.message.RelayMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.ClientMessage.UnsubscribeMessage

interface RelayService {

    @Send
    fun sendSubscribe(msg: SubscribeMessage)

    @Send
    fun sendUnsubscribe(msg: UnsubscribeMessage)

    @Receive
    fun relayMessageFlow(): Flowable<RelayMessage>

    @Receive
    fun webSocketEventFlow(): Flowable<WebSocket.Event>

}

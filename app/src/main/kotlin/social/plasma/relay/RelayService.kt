package social.plasma.relay

import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Flowable
import social.plasma.relay.message.UnsubscribeMessage
import social.plasma.relay.message.RelayMessage
import social.plasma.relay.message.SubscribeMessage

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

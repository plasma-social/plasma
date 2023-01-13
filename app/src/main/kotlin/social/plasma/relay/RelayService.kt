package social.plasma.relay

import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Flowable
import social.plasma.relay.message.CloseMessage
import social.plasma.relay.message.RelayMessage
import social.plasma.relay.message.RequestMessage

interface RelayService {

    @Send
    fun sendSubscribe(msg: RequestMessage)

    @Send
    fun sendClose(msg: CloseMessage)

    @Receive
    fun relayMessageFlow(): Flowable<RelayMessage>

    @Receive
    fun webSocketEventFlow(): Flowable<WebSocket.Event>

}

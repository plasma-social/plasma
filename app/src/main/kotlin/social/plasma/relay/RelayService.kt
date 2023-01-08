package social.plasma.relay

import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Flowable
import kotlinx.coroutines.flow.Flow

interface RelayService {

    @Send
    fun sendSubscribe(msg: RequestMessage)

    @Receive
    fun relayMessageFlow(): Flowable<RelayMessage>

    @Receive
    fun webSocketEventFlow(): Flowable<WebSocket.Event>

}

package social.plasma.relay

import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Flowable

interface RelayService {

    @Send
    fun sendSubscribe(msg: RequestMessage)

    @Receive
    fun observeEvents(): Flowable<Event>
}
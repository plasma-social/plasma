package social.plasma.relay

import com.tinder.scarlet.ws.Send

interface RelayService {

    @Send
    fun sendSubscribe(msg: RequestMessage)

//    @Receive
//    fun observeNotes(): Flowable<Note>
}
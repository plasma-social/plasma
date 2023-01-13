package social.plasma.relay

import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import okhttp3.OkHttpClient
import social.plasma.relay.message.EventRefiner
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Relays @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val scarletBuilder: Scarlet.Builder,
    private val eventRefiner: EventRefiner,
) {
    fun relay(url: String): Relay = Relay(
        scarletBuilder
            .webSocketFactory(okHttpClient.newWebSocketFactory(url))
            .build()
            .create(),
        eventRefiner
    )
}
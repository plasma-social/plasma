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

    companion object {
        val relayUrlList = listOf(
            "wss://brb.io",
            "wss://relay.damus.io",
            "wss://relay.nostr.bg",
            "wss://nostr.oxtr.dev",
            "wss://nostr.v0l.io",
            "wss://nostr-pub.semisol.dev",
            "wss://relay.kronkltd.net",
            "wss://nostr.zebedee.cloud",
            "wss://no.str.cr",
            "wss://relay.nostr.info",
        )
    }
}
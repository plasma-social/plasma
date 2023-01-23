package social.plasma.nostr.relay

import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import okhttp3.OkHttpClient
import social.plasma.nostr.relay.message.RelayMessage.EventRelayMessage
import social.plasma.nostr.relay.message.SubscribeMessage
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class Relays @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val scarletBuilder: Scarlet.Builder,
    @Named("default-relay-list") relayUrlList: List<String>,
) : Relay {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val relayList: List<Relay> = relayUrlList.map { createRelay(it, scope) }

    init {
        connect()
    }

    override val connectionStatus: Flow<Relay.RelayStatus>
        get() = relayList.map { it.connectionStatus }.merge()

    override fun connect() {
        relayList.forEach {
            it.connect()
        }
    }

    override fun disconnect() {
        relayList.forEach { it.disconnect() }
    }

    override fun subscribe(subscribeMessage: SubscribeMessage): Flow<EventRelayMessage> {
        return relayList.map { it.subscribe(subscribeMessage) }.merge()
    }

    private fun createRelay(url: String, scope: CoroutineScope): Relay = RelayImpl(
        url,
        scarletBuilder
            .webSocketFactory(okHttpClient.newWebSocketFactory(url))
            .build()
            .create(),
        scope
    )
}

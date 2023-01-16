package social.plasma.relay

import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import social.plasma.models.TypedEvent
import social.plasma.relay.message.Filters
import social.plasma.relay.message.RelayMessage
import social.plasma.relay.message.SubscribeMessage
import social.plasma.relay.message.UnsubscribeMessage
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Relays @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val scarletBuilder: Scarlet.Builder,
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val relayList: List<Relay> = relayUrlList.map { createRelay(it, scope) }

    private val relayFlows: List<Flow<List<RelayMessage.EventRelayMessage>>> =
        relayList.map { relay ->
            relay.flowRelayMessages().runningFold(emptyList()) { accumulator, value ->
                accumulator + value
            }
        }

    init {
        scope.launch {
            relayList.forEach {
                it.subscribe(SubscribeMessage(filters = Filters.globalFeedNotes))
                it.connect()
            }
        }
    }

    fun subscribe(request: SubscribeMessage): List<UnsubscribeMessage> =
        relayList.map { it.subscribe(request) }

    // TODO purge this list to prevent Out of Memory errors
    fun <T> sharedFlow(
        f: (RelayMessage.EventRelayMessage) -> TypedEvent<T>?,
    ): SharedFlow<List<TypedEvent<T>>> =
        combine(relayFlows.map { it.map { xs -> xs.mapNotNull(f) } }) { values ->
            values.fold(TreeSet<TypedEvent<T>> { l, r ->
                r.createdAt.compareTo(l.createdAt)
            }) { acc, list ->
                acc.addAll(list)
                acc
            }.toList()
        }.shareIn(scope, SharingStarted.Eagerly, replay = 1)

    private fun createRelay(url: String, scope: CoroutineScope): Relay = Relay(
        url,
        scarletBuilder
            .webSocketFactory(okHttpClient.newWebSocketFactory(url))
            .build()
            .create(),
        scope
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

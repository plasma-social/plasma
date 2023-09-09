package social.plasma.nostr.relay

import android.net.Uri
import app.cash.nostrino.crypto.SecKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import social.plasma.data.daos.RelayInfoDao
import social.plasma.nostr.relay.message.ClientMessage.EventMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.ClientMessage.UnsubscribeMessage
import social.plasma.nostr.relay.message.RelayMessage
import social.plasma.nostr.relay.message.RelayMessage.CountRelayMessage
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

interface RelayManager {
    val relayList: StateFlow<List<Relay>>
    val relayUrls: StateFlow<List<String>>
    val relayMessages: Flow<RelayMessage>
    val countMessages: Flow<CountRelayMessage>

    suspend fun sendNote(
        text: String,
        secKey: SecKey,
        tags: Set<List<String>>,
    )

    suspend fun send(event: EventMessage)

    fun subscribe(subscribeMessage: SubscribeMessage): UnsubscribeMessage

    fun unsubscribe(unsubscribeMessage: UnsubscribeMessage)

    fun sendCountRequest(subscribeMessage: SubscribeMessage)
}

@Singleton
class RealRelayManager @Inject constructor(
    private val relayInfoDao: RelayInfoDao,
    @Named("default-relay-list") initialRelayUrls: List<String>,
) : RelayManager {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val defaultRelays = initialRelayUrls.map { url -> RelayInfo(url) }
    private val activeSubscriptions: AtomicReference<Map<String, SubscribeMessage>> =
        AtomicReference(emptyMap())

    private val relays: MutableStateFlow<Map<String, Relay>> = MutableStateFlow(emptyMap())
    private val countRelay = createRelay(
        "wss://relay.nostr.band",
        write = false,
        read = true,
        supportedNips = setOf(Nip.EventCount)
    )

    override val relayList: StateFlow<List<Relay>>
        get() = relays.mapLatest { it.values.toList() }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val relayUrls: StateFlow<List<String>> = relayList.mapLatest { relayList ->
        relayList.map { relay -> relay.url }
    }.stateIn(scope, SharingStarted.Eagerly, initialRelayUrls)

    override val relayMessages: Flow<RelayMessage> = relayList.flatMapLatest { relayList ->
        relayList.map { relay -> relay.relayMessages }.merge()
    }

    override val countMessages: Flow<CountRelayMessage> =
        countRelay.relayMessages.filterIsInstance()

    init {
        scope.launch {
            relayInfoDao.observeRelays().collect { relayList ->
                if (relayList.isEmpty()) {
                    replaceRelayList(defaultRelays)
                } else {
                    replaceRelayList(relayList.map { RelayInfo(it.url, it.read, it.write) })
                }
                resubscribeAll()
            }
        }

        scope.launch {
            countRelay.connect()
        }
    }

    private fun resubscribeAll() {
        activeSubscriptions.get().values.forEach { subscribeMessage ->
            relays.value.forEach { (_, relay) ->
                scope.launch {
                    relay.subscribe(subscribeMessage)
                }
            }
        }
    }

    override fun subscribe(subscribeMessage: SubscribeMessage): UnsubscribeMessage {
        activeSubscriptions.getAndUpdate { it + (subscribeMessage.subscriptionId to subscribeMessage) }
        relays.value.values.forEach { it.subscribe(subscribeMessage) }

        return UnsubscribeMessage(subscribeMessage.subscriptionId)
    }

    override fun unsubscribe(unsubscribeMessage: UnsubscribeMessage) {
        activeSubscriptions.getAndUpdate { it - unsubscribeMessage.subscriptionId }
        relays.value.values.forEach { it.unsubscribe(unsubscribeMessage) }
    }

    override fun sendCountRequest(subscribeMessage: SubscribeMessage) {
        countRelay.sendCountRequest(subscribeMessage)
    }

    override suspend fun send(event: EventMessage) {
        relays.value.values.forEach { it.send(event) }
    }

    override suspend fun sendNote(
        text: String,
        secKey: SecKey,
        tags: Set<List<String>>,
    ) {
        relays.value.values.forEach { it.sendNote(text, secKey, tags) }
    }

    private fun addAndConnectToRelay(relayInfo: RelayInfo): Relay {
        var relay = relays.value[relayInfo.url] // check if relay is already added
        if (relay == null) {
            relay = createRelay(
                url = relayInfo.url,
                read = relayInfo.read,
                write = relayInfo.write
            )
            relays.update { currentRelays -> currentRelays + (relayInfo.url to relay) }
            relay.connect()
        }
        return relay
    }

    private fun createRelay(
        url: String,
        read: Boolean = true,
        write: Boolean = true,
        supportedNips: Set<Nip> = emptySet(),
    ): RelayImpl =
        RelayImpl(
            url = url,
            canRead = read,
            canWrite = write,
            supportedNips = supportedNips,
        )

    private fun replaceRelayList(entities: List<RelayInfo>) {
        val urls = entities.map { it.url }
        removeRelaysNotInNewList(urls)
        addNewRelaysFromList(entities)
    }

    private fun removeRelaysNotInNewList(urls: List<String>) {
        val currentRelays = relays.value
        currentRelays.values.forEach { relay ->
            if (!urls.contains(relay.url)) {
                scope.launch {
                    relay.disconnect()
                }
            }
        }
        relays.update { it.filterKeys { url -> urls.contains(url) } }
    }

    private fun addNewRelaysFromList(entities: List<RelayInfo>) {
        entities.forEach { relayInfo ->
            if (relayInfo.url.isValidSocketUrl) {
                addAndConnectToRelay(relayInfo)
            }
        }
    }
}

private inline val String.isValidSocketUrl: Boolean
    get() {
        return try {
            val uri = Uri.parse(this)
            uri.scheme == "wss" || uri.scheme == "ws"
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

data class RelayInfo(
    val url: String,
    val read: Boolean = true,
    val write: Boolean = true,
)


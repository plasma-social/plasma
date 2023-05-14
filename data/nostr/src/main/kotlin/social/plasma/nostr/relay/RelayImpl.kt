package social.plasma.nostr.relay

import app.cash.nostrino.crypto.SecKey
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import social.plasma.models.Event
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.ClientMessage.EventMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.ClientMessage.UnsubscribeMessage
import social.plasma.nostr.relay.message.RelayMessage
import timber.log.Timber
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

class RelayImpl(
    override val url: String,
    private val service: RelayService,
    private val scope: CoroutineScope,
    override val canRead: Boolean = true,
    override val canWrite: Boolean = true,
) : Relay {

    private val logger
        get() = Timber.tag("relay-$url")

    private val subscriptions = AtomicReference(setOf<SubscribeMessage>())
    private val pendingSendEvents = AtomicReference(listOf<ClientMessage>())

    private var connectionLoop: Job? = null
    private var messagesJob: Job? = null
    private var relayStatusJob: Job? = null

    private val _connectionStatus: MutableStateFlow<Relay.RelayStatus> =
        MutableStateFlow(Relay.RelayStatus(url, Relay.Status.Initial))
    override val connectionStatus: StateFlow<Relay.RelayStatus> = _connectionStatus.asStateFlow()

    private val _relayMessages = MutableSharedFlow<RelayMessage>(0, 500)
    override val relayMessages = _relayMessages.asSharedFlow()

    override fun subscribe(subscribeMessage: SubscribeMessage) {
        if (!canRead) {
            logger.i("Not allowed to read")
        }

        updateSubscriptions(subscribeMessage)
        sendSubscribeMessageIfConnected(subscribeMessage)
        logSubscriptionDetails(subscribeMessage)
    }

    private fun Flow<RelayMessage>.handleNoticeRelayMessage() = onEach {
        if (it is RelayMessage.NoticeRelayMessage) logger.w(it.message)
    }


    private fun updateSubscriptions(subscribeMessage: SubscribeMessage) {
        subscriptions.getAndUpdate { it + subscribeMessage }
    }

    private fun sendSubscribeMessageIfConnected(subscribeMessage: SubscribeMessage) {
        if (_connectionStatus.value.status == Relay.Status.Connected) service.sendSubscribe(
            subscribeMessage
        )
        else pendingSendEvents.updateAndGet { it + subscribeMessage }
    }

    private fun logSubscriptionDetails(subscribeMessage: SubscribeMessage) {
        logger.d("adding sub %s", subscribeMessage)
        logger.d("sub count %s", subscriptions.get().size)
    }

    override suspend fun send(event: EventMessage) {
        if (!canWrite) {
            logger.i("Not allowed to write")
            return
        }
        sendEventIfConnected(event)
    }

    private fun sendEventIfConnected(event: EventMessage) {
        if (_connectionStatus.value.status == Relay.Status.Connected) service.sendEvent(event)
        else pendingSendEvents.updateAndGet { it + event }
    }

    override suspend fun sendNote(
        text: String,
        secKey: SecKey,
        tags: Set<List<String>>,
    ) = send(createEventMessage(text, secKey, tags))

    private fun createEventMessage(
        text: String,
        secKey: SecKey,
        tags: Set<List<String>>,
    ) = EventMessage(
        Event.createEvent(
            secKey.pubKey.key,
            secKey.key,
            Instant.now(),
            Event.Kind.Note,
            tags.toList(),
            text
        )
    )

    override fun unsubscribe(unsubscribeMessage: UnsubscribeMessage) {
        service.sendUnsubscribe(unsubscribeMessage)
        subscriptions.getAndUpdate {
            it.filterNot { sub -> sub.subscriptionId == unsubscribeMessage.subscriptionId }.toSet()
        }
        logUnsubscriptionDetails(unsubscribeMessage)
    }

    private fun logUnsubscriptionDetails(request: UnsubscribeMessage) {
        logger.d("removing sub %s", request)
        logger.d("sub count %s", subscriptions.get().size)
    }

    private fun publishPendingEvents() {
        pendingSendEvents.getAndSet(emptyList()).forEach {
            when (it) {
                is EventMessage -> service.sendEvent(it)
                is SubscribeMessage -> service.sendSubscribe(it)
                is UnsubscribeMessage -> service.sendUnsubscribe(it)
            }
        }
    }

    override fun connect() {
        consumeRelayMessages()
        consumeRelayStatus()
        connectionLoop =
            _connectionStatus.onEach { handleConnectionStatusChange(it) }
                .launchIn(scope + CoroutineName("connectionLoop relay-$url"))
    }

    private fun consumeRelayStatus() {
        relayStatusJob?.cancel()
        relayStatusJob = service.webSocketEventChannel()
            .consumeAsFlow()
            .map { Relay.RelayStatus(url, it.toStatus()) }
            .onEach { _connectionStatus.value = it }
            .launchIn(scope + CoroutineName("consumeRelayStatus relay-$url"))
    }

    private fun handleConnectionStatusChange(status: Relay.RelayStatus) {
        when (status.status) {
            Relay.Status.Initial -> logger.d("connection initial: %s", status)
            is Relay.Status.Connected -> handleConnectedStatus(status)
            is Relay.Status.ConnectionClosing -> logger.d(
                "connection closing: %s",
                status.status.shutdownReason
            )

            is Relay.Status.ConnectionClosed -> handleConnectionClosedStatus(status.status)
            is Relay.Status.ConnectionFailed -> logger.d(
                status.status.throwable,
                "connection failed"
            )
        }
    }

    private fun handleConnectedStatus(status: Relay.RelayStatus) {
        logger.d("connection opened: %s", status)
        publishPendingEvents()
        reSubscribeAll()
    }

    private fun handleConnectionClosedStatus(status: Relay.Status.ConnectionClosed) {
        logger.d("connection closed: %s", status.shutdownReason.reason)
        messagesJob?.cancel()
    }

    private fun consumeRelayMessages() {
        messagesJob?.cancel()
        messagesJob = service.relayMessageChannel()
            .consumeAsFlow()
            .handleNoticeRelayMessage()
            .onEach { _relayMessages.emit(it) }
            .launchIn(scope + CoroutineName("consumeRelayMessages relay-$url"))
    }

    override fun disconnect() {
        connectionLoop?.cancel()
        messagesJob?.cancel()
        relayStatusJob?.cancel()
    }

    private fun reSubscribeAll() {
        logger.d("Resubscribing to %s", subscriptions.get())
        subscriptions.get().forEach { service.sendSubscribe(it) }
    }

    private fun WebSocket.Event.toStatus() = when (this) {
        is WebSocket.Event.OnConnectionOpened<*> -> Relay.Status.Connected
        is WebSocket.Event.OnMessageReceived -> Relay.Status.Connected
        is WebSocket.Event.OnConnectionClosing -> Relay.Status.ConnectionClosing(
            Relay.Status.ShutdownReason(
                code = shutdownReason.code,
                reason = shutdownReason.reason
            )
        )

        is WebSocket.Event.OnConnectionClosed -> Relay.Status.ConnectionClosed(
            Relay.Status.ShutdownReason(
                code = shutdownReason.code,

                reason = shutdownReason.reason
            )
        )

        is WebSocket.Event.OnConnectionFailed -> Relay.Status.ConnectionFailed(throwable)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RelayImpl

        if (url != other.url) return false
        if (canRead != other.canRead) return false
        return canWrite == other.canWrite
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + canRead.hashCode()
        result = 31 * result + canWrite.hashCode()
        return result
    }
}

package social.plasma.nostr.relay

import app.cash.nostrino.crypto.SecKey
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
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
    private val url: String,
    private val service: RelayService,
    private val scope: CoroutineScope,
) : Relay {

    private val tag = "relay-$url"
    private val logger get() = Timber.tag(tag)

    override val connectionStatus: Flow<Relay.RelayStatus> = service.webSocketEventChannel()
        .consumeAsFlow()
        .filterNot { it is WebSocket.Event.OnMessageReceived }
        .mapLatest { Relay.RelayStatus(url, it.toStatus()) }

    private val subscriptions = AtomicReference<Set<SubscribeMessage>>(setOf())
    private val pendingSendEvents = AtomicReference<List<ClientMessage>>(listOf())
    private val status = MutableStateFlow<Relay.Status?>(null)

    override val relayMessages =
        MutableSharedFlow<RelayMessage>(replay = 0, extraBufferCapacity = 500)

    private var connectionLoop: Job? = null
    private var messagesJob: Job? = null

    override fun subscribe(subscribeMessage: SubscribeMessage): Flow<RelayMessage.EventRelayMessage> {
        subscriptions.getAndUpdate { it.plus(subscribeMessage) }

        if (status.value == Relay.Status.Connected) service.sendSubscribe(subscribeMessage)
        else pendingSendEvents.updateAndGet { it.plus(subscribeMessage) }

        logger.d("adding sub %s", subscribeMessage)
        logger.d("sub count %s", subscriptions.get().count())

        return relayMessages
            .onEach { if (it is RelayMessage.NoticeRelayMessage) logger.w(it.message) }
            .filterIsInstance<RelayMessage.EventRelayMessage>()
            .filter { it.subscriptionId == subscribeMessage.subscriptionId }
            .onCompletion {
                unsubscribe(UnsubscribeMessage(subscribeMessage.subscriptionId))
            }
    }

    override suspend fun send(event: EventMessage) {
        if (status.value == Relay.Status.Connected) service.sendEvent(event)
        else pendingSendEvents.updateAndGet { it.plus(event) }
    }

    override suspend fun sendNote(
        text: String,
        secKey: SecKey,
        tags: Set<List<String>>,
    ) =
        send(
            EventMessage(
                Event.createEvent(
                    secKey.pubKey.key,
                    secKey.key,
                    Instant.now(),
                    Event.Kind.Note,
                    tags.toList(),
                    text
                )
            )
        )

    private fun unsubscribe(request: UnsubscribeMessage) {
        service.sendUnsubscribe(request)
        subscriptions.getAndUpdate { set ->
            set.filterNot { it.subscriptionId == request.subscriptionId }.toSet()
        }
        logger.d("removing sub %s", request)
        logger.d("sub count %s", subscriptions.get().count())
    }

    private suspend fun publishPendingEvents() {
        pendingSendEvents.getAndSet(emptyList()).forEach {
            when (it) {
                is EventMessage -> service.sendEvent(it)
                is SubscribeMessage -> service.sendSubscribe(it)
                else -> {}
            }
        }
    }

    override suspend fun connect() {
        connectionLoop = connectionStatus.distinctUntilChanged().onEach {
            status.emit(it.status)
            when (it.status) {
                is Relay.Status.Connected -> {
                    logger.d("connection opened: %s", it)
                    consumeRelayMessages()
                    publishPendingEvents()
                    reSubscribeAll() // TODO - do we need to resubscribe on each reconnect?
                }

                is Relay.Status.ConnectionClosing -> {
                    logger.d("connection closing: %s", it.status.shutdownReason)
                }

                is Relay.Status.ConnectionClosed -> {
                    logger.d("connection closed: %s", it.status.shutdownReason)
                    messagesJob?.cancel()
                }

                is Relay.Status.ConnectionFailed -> {
                    logger.d(it.status.throwable, "connection failed")
                }
            }
        }.launchIn(scope)
    }

    private fun consumeRelayMessages() {
        messagesJob?.cancel()
        messagesJob = service.relayMessageChannel()
            .consumeAsFlow()
            .filterIsInstance<RelayMessage.EventRelayMessage>()
            .onEach { relayMessages.emit(it) }
            .launchIn(scope)
    }

    override fun disconnect() {
        connectionLoop?.cancel()
    }

    private fun reSubscribeAll() {
        logger.d("Resubscribing to %s", subscriptions.get())
        subscriptions.get().parallelStream().forEach { service.sendSubscribe(it) }
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
}

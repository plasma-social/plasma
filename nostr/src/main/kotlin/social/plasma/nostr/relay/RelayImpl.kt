package social.plasma.nostr.relay

import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import social.plasma.crypto.KeyPair
import social.plasma.nostr.models.Event
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.ClientMessage.*
import social.plasma.nostr.relay.message.RelayMessage
import timber.log.Timber
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

class RelayImpl(
    url: String,
    private val service: RelayService,
    private val scope: CoroutineScope,
) : Relay {

    private val tag = "relay-$url"
    private val logger get() = Timber.tag(tag)

    override val connectionStatus: Flow<Relay.RelayStatus> =
        service.webSocketEventFlow().asFlow()
            .filterNot { it is WebSocket.Event.OnMessageReceived }
            .map { Relay.RelayStatus(url, it.toStatus()) }

    private val relayMessages = service.relayMessageFlow()
    private val subscriptions: AtomicReference<Set<SubscribeMessage>> = AtomicReference(setOf())
    private val pendingSendEvents: AtomicReference<List<ClientMessage>> = AtomicReference(listOf())
    private val status = MutableStateFlow<Relay.Status?>(null)

    private var connectionLoop: Job? = null

    override fun subscribe(subscribeMessage: SubscribeMessage): Flow<RelayMessage.EventRelayMessage> {
        subscriptions.getAndUpdate { it.plus(subscribeMessage) }

        if (status.value == Relay.Status.Connected) service.sendSubscribe(subscribeMessage)
        else pendingSendEvents.updateAndGet { it.plus(subscribeMessage) }

        logger.d("adding sub %s", subscribeMessage)
        logger.d("sub count %s", subscriptions.get().count())

        return relayMessages.asFlow()
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

    override suspend fun sendNote(text: String, keyPair: KeyPair, tags: Set<List<String>>) =
        send(
            EventMessage(
                Event.createEvent(
                    keyPair.pub,
                    keyPair.sec,
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
                    publishPendingEvents()
                    reSubscribeAll() // TODO - do we need to resubscribe on each reconnect?
                }

                is Relay.Status.ConnectionClosing -> {
                    logger.d("connection closing: %s", it.status.shutdownReason)
                }

                is Relay.Status.ConnectionClosed -> {
                    logger.d("connection closed: %s", it.status.shutdownReason)
                }

                is Relay.Status.ConnectionFailed -> {
                    logger.d(it.status.throwable, "connection failed")
                }
            }
        }.launchIn(scope)
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

package social.plasma.nostr.relay

import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactive.asFlow
import social.plasma.nostr.relay.message.RelayMessage
import social.plasma.nostr.relay.message.SubscribeMessage
import social.plasma.nostr.relay.message.UnsubscribeMessage
import timber.log.Timber
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

    private val relayMessages = service.relayMessageFlow().hide()

    private val subscriptions: AtomicReference<Set<SubscribeMessage>> =
        AtomicReference(setOf())
    private var connectionLoop: Job? = null

    override fun subscribe(subscribeMessage: SubscribeMessage): Flow<RelayMessage.EventRelayMessage> {
        subscriptions.getAndUpdate { it.plus(subscribeMessage) }

        service.sendSubscribe(subscribeMessage)
        logger.d("adding sub %s", subscribeMessage)

        return relayMessages.asFlow()
            .onEach { if (it is RelayMessage.NoticeRelayMessage) logger.w(it.message) }
            .filterIsInstance<RelayMessage.EventRelayMessage>()
            .filter { it.subscriptionId == subscribeMessage.subscriptionId }
            .onCompletion {
                unsubscribe(UnsubscribeMessage(subscribeMessage.subscriptionId))
            }
    }

    private fun unsubscribe(request: UnsubscribeMessage) {
        service.sendUnsubscribe(request)
        subscriptions.getAndUpdate { set ->
            set.filterNot { it.subscriptionId == request.subscriptionId }.toSet()
        }
        logger.d("removing sub %s", request)
    }

    override fun connect() {
        connectionLoop = connectionStatus.distinctUntilChanged().onEach {
            when (it.status) {
                is Relay.Status.Connected -> {
                    logger.d("connection opened: %s", it)
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
        logger.d("Launched")
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

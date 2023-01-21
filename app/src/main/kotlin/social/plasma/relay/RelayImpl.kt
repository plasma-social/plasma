package social.plasma.relay

import android.util.Log
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
import social.plasma.relay.message.RelayMessage
import social.plasma.relay.message.SubscribeMessage
import social.plasma.relay.message.UnsubscribeMessage
import java.util.concurrent.atomic.AtomicReference

class RelayImpl(
    url: String,
    private val service: RelayService,
    private val scope: CoroutineScope,
) : Relay {
    private val tag = "relay-$url"

    override val connectionStatus: Flow<Relay.RelayStatus> = service.webSocketEventFlow().asFlow()
        .filterNot { it is WebSocket.Event.OnMessageReceived }
        .map { Relay.RelayStatus(url, it.toStatus()) }

    private val relayMessages = service.relayMessageFlow()

    private val subscriptions: AtomicReference<Set<SubscribeMessage>> = AtomicReference(setOf())
    private var connectionLoop: Job? = null

    override fun subscribe(subscribeMessage: SubscribeMessage): Flow<RelayMessage.EventRelayMessage> {
        subscriptions.getAndUpdate { it.plus(subscribeMessage) }

        service.sendSubscribe(subscribeMessage)
        Log.d(tag, "adding sub $subscribeMessage")

        return relayMessages.asFlow()
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
        Log.d(tag, "removing sub $request")
    }

    override fun connect() {
        connectionLoop = connectionStatus.distinctUntilChanged().onEach {
            when (it.status) {
                is Relay.Status.Connected -> {
                    Log.d(tag, "connection opened: $it")
                    reSubscribeAll() // TODO - do we need to resubscribe on each reconnect?
                }

                is Relay.Status.ConnectionClosing -> {
                    Log.d(tag, "connection closing: ${it.status.shutdownReason}")
                }

                is Relay.Status.ConnectionClosed -> {
                    Log.d(tag, "connection closed: ${it.status.shutdownReason}")
                }

                is Relay.Status.ConnectionFailed -> {
                    Log.d(tag, "connection failed", it.status.throwable)
                }
            }
        }.launchIn(scope)
        Log.d(tag, "Launched")
    }

    override fun disconnect() {
        connectionLoop?.cancel()
    }

    private fun reSubscribeAll() {
        Log.d(tag, "Resubscribing to ${subscriptions.get()}")
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

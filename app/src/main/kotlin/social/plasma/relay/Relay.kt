package social.plasma.relay

import android.util.Log
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactive.asFlow
import social.plasma.relay.message.RelayMessage.EventRelayMessage
import social.plasma.relay.message.SubscribeMessage
import social.plasma.relay.message.UnsubscribeMessage
import java.util.concurrent.atomic.AtomicReference

class Relay(
    url: String,
    private val service: RelayService,
    private val scope: CoroutineScope,
) {

    private val connectionStatus = service.webSocketEventFlow().asFlow()
        .filterNot { it is WebSocket.Event.OnMessageReceived }

    private val tag = "relay-$url"
    private val subscriptions: AtomicReference<Set<SubscribeMessage>> = AtomicReference(setOf())
    private var connectionLoop: Job? = null

    fun subscribe(request: SubscribeMessage): UnsubscribeMessage {
        Log.d(tag, "adding sub $request. subscriptions now: ${
            subscriptions.getAndUpdate { it.plus(request) }
        }")
        service.sendSubscribe(request) // no-op if not connected
        return UnsubscribeMessage(request.subscriptionId)
    }

    fun unsubscribe(request: UnsubscribeMessage) {
        service.sendUnsubscribe(request)
        subscriptions.getAndUpdate { set ->
            set.filterNot { it.subscriptionId == request.subscriptionId }.toSet()
        }
        Log.d(tag, "unsubscribed. subs now ${subscriptions.get()}")
    }

    fun connect(): Relay {
        connectionLoop = connectionStatus.onEach {
            when (it) {
                is WebSocket.Event.OnConnectionOpened<*> -> {
                    Log.d(tag, "connection opened: $it")
                    reSubscribeAll() // TODO - do we need to resubscribe on each reconnect?
                }

                is WebSocket.Event.OnConnectionClosing -> {
                    Log.d(tag, "connection closing: ${it.shutdownReason}")
                }

                is WebSocket.Event.OnConnectionClosed -> {
                    Log.d(tag, "connection closed: ${it.shutdownReason}")
                }

                is WebSocket.Event.OnConnectionFailed -> {
                    Log.d(tag, "connection failed", it.throwable)
                }

                else -> {}
            }
        }.launchIn(scope)
        Log.d(tag, "Launched")
        return this
    }

    fun disconnect() {
        connectionLoop?.cancel()
    }

    private fun reSubscribeAll() {
        Log.d(tag, "Resubscribing to ${subscriptions.get()}")
        subscriptions.get().parallelStream().forEach { service.sendSubscribe(it) }
    }

    /** All events sent by the relay to the client */
    fun flowRelayMessages(): Flow<EventRelayMessage> = service.relayMessageFlow()
        .asFlow()
        .filterIsInstance()

}

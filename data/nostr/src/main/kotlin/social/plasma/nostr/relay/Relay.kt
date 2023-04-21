package social.plasma.nostr.relay

import app.cash.nostrino.crypto.SecKey
import kotlinx.coroutines.flow.Flow
import social.plasma.nostr.relay.message.ClientMessage.EventMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.RelayMessage
import social.plasma.nostr.relay.message.RelayMessage.EventRelayMessage

interface Relay {
    val connectionStatus: Flow<RelayStatus>

    val relayMessages: Flow<RelayMessage>

    suspend fun connect()

    fun disconnect()

    fun subscribe(subscribeMessage: SubscribeMessage): Flow<EventRelayMessage>

    suspend fun send(event: EventMessage)
    suspend fun sendNote(
        text: String,
        secKey: SecKey,
        tags: Set<List<String>> = emptySet(),
    )

    data class RelayStatus(
        val url: String,
        val status: Status,
    )

    sealed interface Status {
        object Connected : Status

        data class ConnectionClosing(val shutdownReason: ShutdownReason) : Status

        data class ConnectionClosed(val shutdownReason: ShutdownReason) : Status

        data class ConnectionFailed(val throwable: Throwable) : Status

        data class ShutdownReason(
            val code: Int,
            val reason: String,
        )
    }
}



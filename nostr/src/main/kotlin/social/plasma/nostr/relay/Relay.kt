package social.plasma.nostr.relay

import kotlinx.coroutines.flow.Flow
import social.plasma.nostr.relay.message.RelayMessage.EventRelayMessage
import social.plasma.nostr.relay.message.SubscribeMessage

interface Relay {
    val connectionStatus: Flow<RelayStatus>

    fun connect()

    fun disconnect()

    fun subscribe(subscribeMessage: SubscribeMessage): Flow<EventRelayMessage>

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



package social.plasma.nostr.relay

import app.cash.nostrino.crypto.SecKey
import kotlinx.coroutines.flow.Flow
import social.plasma.nostr.relay.message.ClientMessage.EventMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.ClientMessage.UnsubscribeMessage
import social.plasma.nostr.relay.message.RelayMessage

interface Relay {
    val canRead: Boolean
    val canWrite: Boolean
    val supportedNips: Set<Nip>
    val url: String

    val connectionStatus: Flow<RelayStatus>

    val relayMessages: Flow<RelayMessage>

    fun connect()

    fun disconnect()

    fun subscribe(subscribeMessage: SubscribeMessage)

    fun unsubscribe(unsubscribeMessage: UnsubscribeMessage)

    suspend fun send(event: EventMessage)
    suspend fun sendNote(
        text: String,
        secKey: SecKey,
        tags: Set<List<String>> = emptySet(),
    )

    fun sendCountRequest(subscribeMessage: SubscribeMessage)

    data class RelayStatus(
        val url: String,
        val status: Status,
    )

    sealed interface Status {
        object Initial : Status

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



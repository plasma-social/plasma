package social.plasma.repository

import app.cash.turbine.Turbine
import kotlinx.coroutines.flow.Flow
import social.plasma.crypto.KeyPair
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.RelayMessage

// TODO move to a common module
class FakeRelay : Relay {
    val sendNoteTurbine = Turbine<SendNote>()

    override val connectionStatus: Flow<Relay.RelayStatus>
        get() = TODO("Not yet implemented")

    override suspend fun connect() {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }

    override fun subscribe(subscribeMessage: ClientMessage.SubscribeMessage): Flow<RelayMessage.EventRelayMessage> {
        TODO("Not yet implemented")
    }

    override suspend fun send(event: ClientMessage.EventMessage) {
        TODO("Not yet implemented")
    }

    override suspend fun sendNote(text: String, keyPair: KeyPair, tags: Set<List<String>>) {
        sendNoteTurbine.add(SendNote(text = text, keyPair = keyPair, tags = tags))
    }

    data class SendNote(val text: String, val keyPair: KeyPair, val tags: Set<List<String>>)
}

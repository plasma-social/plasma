package social.plasma.data.nostr.fakes

import app.cash.nostrino.crypto.SecKey
import app.cash.turbine.Turbine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.RelayManager
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.RelayMessage

// TODO move to a common module
class FakeRelayManager : RelayManager {
    val sendNoteTurbine = Turbine<SendNote>()
    val sendEventTurbine = Turbine<ClientMessage.EventMessage>()

    override val relayList: StateFlow<List<Relay>>
        get() = TODO("Not yet implemented")
    override val relayMessages: Flow<RelayMessage>
        get() = emptyFlow()


    override fun subscribe(subscribeMessage: ClientMessage.SubscribeMessage): ClientMessage.UnsubscribeMessage {
        return ClientMessage.UnsubscribeMessage("")
    }

    override fun unsubscribe(unsubscribeMessage: ClientMessage.UnsubscribeMessage) {
        
    }

    override suspend fun send(event: ClientMessage.EventMessage) {
        sendEventTurbine.add(event)
    }

    override suspend fun sendNote(text: String, secKey: SecKey, tags: Set<List<String>>) {
        sendNoteTurbine.add(SendNote(text = text, secKey = secKey, tags = tags))
    }

    data class SendNote(val text: String, val secKey: SecKey, val tags: Set<List<String>>)
}

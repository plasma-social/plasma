package social.plasma.nostr.relay.message

import social.plasma.models.Event
import social.plasma.models.EventCount

/** A message sent from a relay to a client. It may wrap an event or be a notice */
sealed class RelayMessage {

    /** Wraps a nip-01 event destined for the given subscription */
    data class EventRelayMessage(
        val subscriptionId: String,
        val event: Event,
    ) : RelayMessage()

    /** A notice from the relay to the client */
    data class NoticeRelayMessage(
        val message: String,
    ) : RelayMessage()

    data class CountRelayMessage(
        val subscriptionId: String,
        val count: EventCount,
    ) : RelayMessage()

    object EOSEMessage : RelayMessage()
}


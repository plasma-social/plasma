package social.plasma.repository

import social.plasma.nostr.models.Contact
import social.plasma.nostr.models.Note
import social.plasma.nostr.models.Reaction
import social.plasma.nostr.models.RelayDetails
import social.plasma.nostr.models.TypedEvent
import social.plasma.nostr.models.UserMetaData
import social.plasma.nostr.relay.message.EventRefiner
import social.plasma.nostr.relay.message.RelayMessage

// TODO move to common module
class FakeEventRefiner : EventRefiner {
    override fun toUserMetaData(message: RelayMessage.EventRelayMessage): TypedEvent<UserMetaData>? {
        TODO("Not yet implemented")
    }

    override fun toNote(message: RelayMessage.EventRelayMessage): TypedEvent<Note>? {
        TODO("Not yet implemented")
    }

    override fun toContacts(message: RelayMessage.EventRelayMessage): TypedEvent<Set<Contact>>? {
        TODO("Not yet implemented")
    }

    override fun toRelayDetailList(message: RelayMessage.EventRelayMessage): TypedEvent<Map<String, RelayDetails>>? {
        TODO("Not yet implemented")
    }

    override fun toReaction(relayMessage: RelayMessage.EventRelayMessage): TypedEvent<Reaction>? {
        TODO("Not yet implemented")
    }
}

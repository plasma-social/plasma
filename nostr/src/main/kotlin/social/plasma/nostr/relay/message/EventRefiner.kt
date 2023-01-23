package social.plasma.nostr.relay.message

import com.squareup.moshi.Moshi
import social.plasma.nostr.models.Contact
import social.plasma.nostr.models.Event
import social.plasma.nostr.models.Note
import social.plasma.nostr.models.Reaction
import social.plasma.nostr.models.TypedEvent
import social.plasma.nostr.models.UserMetaData
import social.plasma.nostr.relay.message.RelayMessage.EventRelayMessage
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
/** Filters and refines an event relay message flow into a flows of more concrete event types */
class EventRefiner @Inject constructor(
    moshi: Moshi,
) {

    private val userMetaDataAdapter = moshi.adapter(UserMetaData::class.java)

    fun toUserMetaData(message: EventRelayMessage): TypedEvent<UserMetaData>? =
        message.event.takeIf { it.kind == Event.Kind.MetaData }
            ?.let { userMetaDataAdapter.fromJson(it.content) }
            ?.let { message.event.typed(it) }

    fun toNote(message: EventRelayMessage): TypedEvent<Note>? =
        message.event.takeIf { it.kind == Event.Kind.Note }
            ?.let { it.typed(Note(it.content)) }

    fun toContacts(message: EventRelayMessage): TypedEvent<Set<Contact>>? =
        message.event.takeIf { it.kind == Event.Kind.ContactList }
            ?.let { it.typed(it.typed("").contacts()) }

    fun toReaction(relayMessage: EventRelayMessage): TypedEvent<Reaction>? =
        relayMessage.event.takeIf { it.kind == Event.Kind.Reaction }
            ?.let { it.typed(Reaction(it.content)) }

}
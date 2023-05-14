package social.plasma.nostr.relay.message

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import social.plasma.models.Contact
import social.plasma.models.Event
import social.plasma.nostr.models.Note
import social.plasma.nostr.models.Reaction
import social.plasma.nostr.models.RelayDetails
import social.plasma.models.TypedEvent
import social.plasma.nostr.models.UserMetaData
import social.plasma.nostr.relay.message.RelayMessage.EventRelayMessage
import javax.inject.Inject
import javax.inject.Singleton

interface EventRefiner {
    fun toUserMetaData(message: EventRelayMessage): TypedEvent<UserMetaData>?

    fun toNote(message: EventRelayMessage): TypedEvent<Note>?

    fun toContacts(message: EventRelayMessage): TypedEvent<Set<Contact>>?

    fun toRelayDetailList(message: EventRelayMessage): TypedEvent<Map<String, RelayDetails>>?

    fun toRelayDetailList(event: Event): TypedEvent<Map<String, RelayDetails>>?

    fun toReaction(relayMessage: EventRelayMessage): TypedEvent<Reaction>?
}

@Singleton
/** Filters and refines an event relay message flow into a flows of more concrete event types */
internal class RealEventRefiner @Inject constructor(
    moshi: Moshi,
) : EventRefiner {

    private val userMetaDataAdapter = moshi.adapter(UserMetaData::class.java)
    private val relayDetailsMapType =
        Types.newParameterizedType(Map::class.java, String::class.java, RelayDetails::class.java)
    private val relayDetailsAdapter = moshi.adapter<Map<String, RelayDetails>>(relayDetailsMapType)

    override fun toUserMetaData(message: EventRelayMessage): TypedEvent<UserMetaData>? =
        message.event.takeIf { it.kind == Event.Kind.MetaData }
            ?.let { userMetaDataAdapter.fromJson(it.content) }
            ?.let { message.event.typed(it) }

    override fun toNote(message: EventRelayMessage): TypedEvent<Note>? =
        message.event.takeIf { it.kind == Event.Kind.Note }
            ?.let { it.typed(Note(it.content)) }

    override fun toContacts(message: EventRelayMessage): TypedEvent<Set<Contact>>? =
        message.event.takeIf { it.kind == Event.Kind.ContactList }
            ?.let { it.typed(it.typed("").contacts()) }

    override fun toRelayDetailList(message: EventRelayMessage): TypedEvent<Map<String, RelayDetails>>? =
        toRelayDetailList(message.event)


    override fun toRelayDetailList(event: Event): TypedEvent<Map<String, RelayDetails>>? =
        event.takeIf { it.kind == Event.Kind.ContactList && it.content.isNotEmpty() }
            ?.let { it.typed(relayDetailsAdapter.fromJson(it.content)!!) }


    override fun toReaction(relayMessage: EventRelayMessage): TypedEvent<Reaction>? =
        relayMessage.event.takeIf { it.kind == Event.Kind.Reaction }
            ?.let { it.typed(Reaction(it.content)) }

}

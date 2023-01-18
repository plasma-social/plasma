package social.plasma.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import social.plasma.models.Contact
import social.plasma.models.TypedEvent
import social.plasma.relay.Relays
import social.plasma.relay.message.EventRefiner
import social.plasma.relay.message.Filters
import social.plasma.relay.message.SubscribeMessage
import social.plasma.relay.message.UnsubscribeMessage
import javax.inject.Inject

interface ContactListRepository {
    fun observeContactLists(): Flow<List<TypedEvent<Set<Contact>>>>
}

class RealContactListRepository @Inject constructor(
    private val relays: Relays,
    private val eventRefiner: EventRefiner,
) : ContactListRepository {

    fun requestContactLists(pubKey: String): List<UnsubscribeMessage> =
        relays.subscribe(SubscribeMessage(
            filters = Filters.contactList(pubKey)
        ))

    private val contactListSharedFlow: SharedFlow<List<TypedEvent<Set<Contact>>>> =
        relays.sharedFlow { eventRefiner.toContacts(it) }

    override fun observeContactLists(): Flow<List<TypedEvent<Set<Contact>>>> = contactListSharedFlow

}


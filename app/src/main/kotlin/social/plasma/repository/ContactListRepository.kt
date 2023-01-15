package social.plasma.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import social.plasma.models.Contact
import social.plasma.relay.Relays
import social.plasma.relay.message.Filters
import social.plasma.relay.message.SubscribeMessage
import social.plasma.relay.message.UnsubscribeMessage
import javax.inject.Inject

interface ContactListRepository {
    fun observeContactLists(): Flow<Set<Contact>>
}

class RealContactListRepository @Inject constructor(
    private val relays: Relays,
) : ContactListRepository {

    fun requestContactLists(pubKey: String): List<UnsubscribeMessage> =
        relays.subscribe(
            SubscribeMessage(
                filters = Filters.contactList(pubKey)
            )
        )

    private val contactListFlow: Flow<Set<Contact>> = flowOf()

    override fun observeContactLists(): Flow<Set<Contact>> = contactListFlow

}


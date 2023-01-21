package social.plasma.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import social.plasma.models.Contact
import social.plasma.relay.Relays
import social.plasma.relay.message.UnsubscribeMessage
import javax.inject.Inject

interface ContactListRepository {
    fun observeContactLists(): Flow<Set<Contact>>
}

class RealContactListRepository @Inject constructor(
    private val relays: Relays,
) : ContactListRepository {

    // TODO connect to relay
    fun requestContactLists(pubKey: String): List<UnsubscribeMessage> = TODO()

    private val contactListFlow: Flow<Set<Contact>> = flowOf()

    override fun observeContactLists(): Flow<Set<Contact>> = contactListFlow

}


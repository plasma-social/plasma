package social.plasma.data.daos.fakes

import app.cash.turbine.Turbine
import kotlinx.coroutines.flow.Flow
import social.plasma.data.daos.ContactsDao
import social.plasma.models.ContactEntity
import social.plasma.models.events.EventEntity

class FakeContactsDao : ContactsDao {
    override fun insert(contacts: Iterable<ContactEntity>) {
        TODO("Not yet implemented")
    }

    override fun observeContacts(pubkey: String): Flow<List<ContactEntity>> {
        TODO("Not yet implemented")
    }

    override fun observeOwnerFollowsContact(
        ownerPubKey: String,
        contactPubKey: String,
    ): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override fun observeContactListEvent(pubkey: String): Flow<EventEntity> {
        TODO("Not yet implemented")
    }

    val getContactListEventRequests = Turbine<String>()
    val getContactListEventResponses = Turbine<EventEntity?>()
    override suspend fun getContactListEvent(pubkey: String): EventEntity? {
        getContactListEventRequests.add(pubkey)
        return getContactListEventResponses.awaitItem()
    }

    override fun delete() {
        TODO("Not yet implemented")
    }

    override suspend fun getNewestEvent(pubkey: String): EventEntity? {
        TODO("Not yet implemented")
    }
}

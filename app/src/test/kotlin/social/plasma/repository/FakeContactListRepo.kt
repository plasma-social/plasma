package social.plasma.repository

import kotlinx.coroutines.flow.Flow
import social.plasma.nostr.models.Contact

class FakeContactListRepo : ContactListRepository {
    override fun observeContactLists(pubkey: String): Flow<Set<Contact>> {
        TODO("Not yet implemented")
    }

    override fun observeFollowState(pubKey: String, contactPubKey: String): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override fun observeFollowingCount(pubkey: String): Flow<Long> {
        TODO("Not yet implemented")
    }

    override fun observeFollowersCount(pubkey: String): Flow<Int> {
        TODO("Not yet implemented")
    }

    override fun syncContactList(pubkey: String): Flow<Set<Contact>> {
        TODO("Not yet implemented")
    }

}

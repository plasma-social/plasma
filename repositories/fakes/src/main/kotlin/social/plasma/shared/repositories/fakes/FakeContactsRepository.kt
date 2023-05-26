package social.plasma.shared.repositories.fakes

import social.plasma.shared.repositories.api.ContactsRepository

class FakeContactsRepository : ContactsRepository {
    override suspend fun follow(pubKeyHex: String) {
        TODO("Not yet implemented")
    }

    override suspend fun unfollow(pubKeyHex: String) {
        TODO("Not yet implemented")
    }
}

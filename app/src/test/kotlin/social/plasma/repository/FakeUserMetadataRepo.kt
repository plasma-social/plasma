package social.plasma.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import social.plasma.models.PubKey
import social.plasma.nostr.models.UserMetaData

class FakeUserMetadataRepo : UserMetaDataRepository {
    override fun observeUserMetaData(pubKey: String): Flow<UserMetaData?> {
        return flowOf(null)
    }

    override suspend fun syncUserMetadata(pubKey: String, force: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun stopUserMetadataSync(pubKey: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getById(pubkey: String): UserMetaData? {
        return null
    }

    override suspend fun isNip5Valid(pubKey: PubKey, identifier: String?): Boolean {
        TODO("Not yet implemented")
    }

}

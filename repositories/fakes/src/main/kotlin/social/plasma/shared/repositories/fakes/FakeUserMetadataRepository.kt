package social.plasma.shared.repositories.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import social.plasma.models.PubKey
import social.plasma.models.UserMetadataEntity
import social.plasma.shared.repositories.api.UserMetadataRepository

class FakeUserMetadataRepository : UserMetadataRepository {
    override fun search(query: String): Flow<List<String>> {
        return emptyFlow()
    }

    override fun observeUserMetaData(pubKey: PubKey): Flow<UserMetadataEntity?> {
        return emptyFlow()
    }
}
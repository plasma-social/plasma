package social.plasma.shared.repositories.fakes

import app.cash.nostrino.crypto.PubKey
import app.cash.turbine.Turbine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import social.plasma.models.UserMetadataEntity
import social.plasma.shared.repositories.api.UserMetadataRepository

class FakeUserMetadataRepository : UserMetadataRepository {
    val searchUsersCalls: Turbine<String> = Turbine()

    var searchUsersResult: List<UserMetadataEntity> = emptyList()
    val observeUserMetaDataResult: MutableStateFlow<UserMetadataEntity?> = MutableStateFlow(null)

    override fun observeUserMetaData(pubKey: PubKey): Flow<UserMetadataEntity?> {
        return observeUserMetaDataResult.asStateFlow()
    }

    override suspend fun searchUsers(query: String): List<UserMetadataEntity> {
        searchUsersCalls.add(query)
        return searchUsersResult
    }
}

package social.plasma.shared.repositories.real

import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.flow.Flow
import social.plasma.data.daos.UserMetadataDao
import social.plasma.models.UserMetadataEntity
import social.plasma.shared.repositories.api.UserMetadataRepository
import javax.inject.Inject

internal class RealUserMetadataRepository @Inject constructor(
    private val userMetadataDao: UserMetadataDao,
) : UserMetadataRepository {
    override fun observeUserMetaData(pubKey: PubKey): Flow<UserMetadataEntity?> {
        return userMetadataDao.observeUserMetadata(pubKey.key.hex())
    }

    override suspend fun searchUsers(query: String): List<UserMetadataEntity> {
        val nameQuery = "name:$query* OR displayName:$query*"
        return userMetadataDao.search(nameQuery)
    }
}

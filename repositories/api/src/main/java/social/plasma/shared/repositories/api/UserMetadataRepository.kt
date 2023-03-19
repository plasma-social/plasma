package social.plasma.shared.repositories.api

import kotlinx.coroutines.flow.Flow
import social.plasma.models.PubKey
import social.plasma.models.UserMetadataEntity


interface UserMetadataRepository {
    fun search(query: String): Flow<List<String>>

    fun observeUserMetaData(pubKey: PubKey): Flow<UserMetadataEntity?>
}
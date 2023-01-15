package social.plasma.repository

import kotlinx.coroutines.flow.Flow
import social.plasma.db.usermetadata.UserMetadataDao
import social.plasma.db.usermetadata.UserMetadataEntity
import social.plasma.relay.Relays
import social.plasma.relay.message.Filters
import social.plasma.relay.message.SubscribeMessage
import social.plasma.relay.message.UnsubscribeMessage
import javax.inject.Inject

interface UserMetaDataRepository {
    fun observeUserMetaData(pubKey: String): Flow<UserMetadataEntity>
}

class RealUserMetaDataRepository @Inject constructor(
    private val relays: Relays,
    private val metadataDao: UserMetadataDao,
) : UserMetaDataRepository {

    fun requestUserMetaData(pubKey: String): List<UnsubscribeMessage> =
        relays.subscribe(
            SubscribeMessage(
                filters = Filters.userMetaData(pubKey)
            )
        )

    override fun observeUserMetaData(pubKey: String): Flow<UserMetadataEntity> {
        return metadataDao.observeUserMetadata(pubKey)
    }
}

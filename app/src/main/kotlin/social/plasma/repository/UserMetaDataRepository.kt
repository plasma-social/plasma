package social.plasma.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import social.plasma.db.usermetadata.UserMetadataDao
import social.plasma.db.usermetadata.UserMetadataEntity
import social.plasma.nostr.models.TypedEvent
import social.plasma.nostr.models.UserMetaData
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.EventRefiner
import social.plasma.nostr.relay.message.Filters
import social.plasma.nostr.relay.message.SubscribeMessage
import social.plasma.utils.chunked
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

interface UserMetaDataRepository {
    fun observeUserMetaData(pubKey: String): Flow<UserMetaData>

    fun syncUserMetadata(pubKey: String): Flow<Unit>

    fun syncUserMetadata(pubKeys: Set<String>): Flow<Unit>
}

class RealUserMetaDataRepository @Inject constructor(
    private val relays: Relay,
    private val metadataDao: UserMetadataDao,
    private val eventRefiner: EventRefiner,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : UserMetaDataRepository {
    override fun observeUserMetaData(pubKey: String): Flow<UserMetaData> =
        metadataDao.observeUserMetadata(pubKey)
            .distinctUntilChanged()
            .map {
                it?.let {
                    UserMetaData(
                        name = it.name,
                        displayName = it.displayName,
                        about = it.about,
                        picture = it.picture,
                    )
                }
            }
            .filterNotNull()

    override fun syncUserMetadata(pubKey: String): Flow<Unit> {
        return syncUserMetadata(setOf(pubKey))
    }

    override fun syncUserMetadata(pubKeys: Set<String>): Flow<Unit> {
        return relays.subscribe(
            SubscribeMessage(filters = Filters.userMetaData(pubKeys))
        )
            .distinctUntilChanged()
            .map { eventRefiner.toUserMetaData(it) }
            .filterNotNull()
            .chunked(pubKeys.size, 200)
            .map { metadataList ->
                metadataDao.insertIfNewer(metadataList.map { it.toUserMetadataEntity() })
            }
            .flowOn(ioDispatcher)
    }

}

fun TypedEvent<UserMetaData>.toUserMetadataEntity(): UserMetadataEntity =
    UserMetadataEntity(
        pubkey = pubKey.hex(),
        name = content.name,
        about = content.about,
        picture = content.picture,
        displayName = content.displayName,
        createdAt = createdAt.toEpochMilli(),
    )

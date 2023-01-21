package social.plasma.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import social.plasma.db.usermetadata.UserMetadataDao
import social.plasma.db.usermetadata.UserMetadataEntity
import social.plasma.models.TypedEvent
import social.plasma.models.UserMetaData
import social.plasma.relay.Relays
import social.plasma.relay.message.EventRefiner
import social.plasma.relay.message.Filters
import social.plasma.relay.message.SubscribeMessage
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

interface UserMetaDataRepository {
    fun observeUserMetaData(pubKey: String): Flow<UserMetaData>
}

class RealUserMetaDataRepository @Inject constructor(
    private val relays: Relays,
    private val metadataDao: UserMetadataDao,
    private val eventRefiner: EventRefiner,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : UserMetaDataRepository {
    override fun observeUserMetaData(pubKey: String): Flow<UserMetaData> =
        merge(
            metadataDao.observeUserMetadata(pubKey)
                .map {
                    it?.let {
                        UserMetaData(
                            name = it.name,
                            displayName = it.displayName,
                            about = it.about,
                            picture = it.picture,
                        )
                    }
                },

            relays.subscribe(
                SubscribeMessage(filters = Filters.userMetaData(pubKey))
            )
                .map { eventRefiner.toUserMetaData(it) }
                .filterNotNull()
                .onEach {
                    metadataDao.insert(it.toUserMetadataEntity())
                }
                .flowOn(ioDispatcher)
                .map { it.content }
        ).filterNotNull().distinctUntilChanged()

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

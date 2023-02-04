package social.plasma.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import social.plasma.db.usermetadata.UserMetadataDao
import social.plasma.db.usermetadata.UserMetadataEntity
import social.plasma.nostr.models.Event
import social.plasma.nostr.models.TypedEvent
import social.plasma.nostr.models.UserMetaData
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.EventRefiner
import social.plasma.nostr.relay.message.Filter
import social.plasma.utils.chunked
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

interface UserMetaDataRepository {
    fun observeUserMetaData(pubKey: String): Flow<UserMetaData>

    suspend fun syncUserMetadata(pubKey: String, force: Boolean = false)

    suspend fun stopUserMetadataSync(pubKey: String)
    suspend fun getById(pubkey: String) : UserMetaData?
}

@Singleton
class RealUserMetaDataRepository @Inject constructor(
    private val relays: Relay,
    private val metadataDao: UserMetadataDao,
    private val eventRefiner: EventRefiner,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : UserMetaDataRepository {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val syncedIds = AtomicReference(setOf<String>())
    private val idsToSync = AtomicReference(setOf<String>())
    private val idsToSyncFlow = MutableSharedFlow<Set<String>>()

    init {
        idsToSyncFlow
            .filter { it.isNotEmpty() }
            .distinctUntilChanged()
            .debounce(500)
            .flatMapLatest {
                Timber.d("Request %s users", it.size)
                relays.subscribe(
                    SubscribeMessage(
                        filter = Filter(
                            authors = setOf(it.first()),
                            kinds = setOf(Event.Kind.MetaData),
                            limit = 1,
                        ),
                        *it.drop(1).map {
                            Filter(
                                authors = setOf(it),
                                kinds = setOf(Event.Kind.MetaData),
                                limit = 1,
                            )
                        }.toTypedArray()
                    )
                )
            }
            .map { eventRefiner.toUserMetaData(it) }
            .filterNotNull()
            .chunked(100, 200)
            .map { metadataList ->
                metadataDao.insertIfNewer(metadataList.map { it.toUserMetadataEntity() })
            }
            .launchIn(scope)
    }

    override fun observeUserMetaData(pubKey: String): Flow<UserMetaData> =
        metadataDao.observeUserMetadata(pubKey)
            .distinctUntilChanged()
            .filterNotNull()
            .map { it.toUserMetadata() }

    override suspend fun syncUserMetadata(pubKey: String, force: Boolean) {
        if (force || !syncedIds.get().contains(pubKey)) {
            idsToSync.getAndUpdate { it + pubKey }

            idsToSyncFlow.emit(idsToSync.get())
        }
    }

    override suspend fun stopUserMetadataSync(pubKey: String) {
        idsToSync.getAndUpdate { it - pubKey }
    }

    override suspend fun getById(pubkey: String): UserMetaData? {
        return metadataDao.getById(pubkey).first()?.toUserMetadata()
    }
}

private fun UserMetadataEntity.toUserMetadata(): UserMetaData = UserMetaData(
    name = name,
    displayName = displayName,
    about = about,
    picture = picture,
    banner = banner,
    website = website,
    nip05 = nip05,
)

fun TypedEvent<UserMetaData>.toUserMetadataEntity(): UserMetadataEntity =
    UserMetadataEntity(
        pubkey = pubKey.hex(),
        name = content.name,
        about = content.about,
        picture = content.picture,
        displayName = content.displayName,
        banner = content.banner,
        nip05 = content.nip05?.split("@")?.getOrNull(1), // TODO regex
        website = content.website,
        createdAt = createdAt.epochSecond,
    )

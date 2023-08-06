package social.plasma.domain.interactors

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import social.plasma.data.daos.UserMetadataDao
import social.plasma.domain.SubjectInteractor
import social.plasma.models.Event
import social.plasma.models.UserMetadataEntity
import social.plasma.nostr.models.UserMetaData
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class StoreMetadataEvents @Inject constructor(
    private val userMetadataDao: UserMetadataDao,
    moshi: Moshi,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : SubjectInteractor<Flow<Event>, Any>() {
    private val userMetaDataAdapter = moshi.adapter(UserMetaData::class.java)

    override fun createObservable(params: Flow<Event>): Flow<Any> {
        return params.filter { it.kind == Event.Kind.MetaData }
            .map {
                try {
                    userMetaDataAdapter.fromJson(it.content)
                        ?.toMetadataEntity(it.pubKey.hex(), it.createdAt)
                } catch (e: JsonDataException) {
                    Timber.e(e, "Unexpected json metadata: %s")
                    null
                }
            }
            .catch {
                Timber.w(it)
            }
            .filterNotNull()
            .onEach {
                userMetadataDao.insert(it)
            }.flowOn(ioDispatcher)
    }
}

internal fun UserMetaData.toMetadataEntity(
    pubKey: String,
    createdAt: Instant,
): UserMetadataEntity = UserMetadataEntity(
    pubkey = pubKey,
    name = name,
    about = about,
    picture = picture,
    displayName = displayName,
    banner = banner,
    nip05 = nip05,
    website = website,
    createdAt = createdAt.epochSecond,
    lud06 = lud06,
    lud16 = lud16,
)


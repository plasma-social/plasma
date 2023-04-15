package social.plasma.domain.interactors

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import social.plasma.data.daos.LastRequestDao
import social.plasma.domain.Interactor
import social.plasma.models.LastRequestEntity
import social.plasma.models.Request
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.Filter
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class SyncHashTagEvents @Inject constructor(
    private val relay: Relay,
    private val storeEvents: StoreEvents,
    private val lastRequestDao: LastRequestDao,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : Interactor<SyncHashTagEvents.Params>() {

    override suspend fun doWork(params: Params) = withContext(ioDispatcher) {
        val hashTagWithoutSign = params.hashtag.removePrefix("#").lowercase()

        val lastRequest =
            lastRequestDao.lastRequest(Request.SYNC_HASHTAG, hashTagWithoutSign)?.timestamp
                ?: Instant.EPOCH

        val subscribeMessage = SubscribeMessage(
            Filter(hashTags = setOf(hashTagWithoutSign), since = lastRequest, limit = 250),
        )

        val subscription =
            relay.subscribe(subscribeMessage).distinctUntilChanged().map { it.event }

        storeEvents(subscription)
        storeEvents.flow.onStart {
            lastRequestDao.upsert(
                LastRequestEntity(
                    request = Request.SYNC_HASHTAG,
                    resourceId = hashTagWithoutSign
                )
            )
        }.collect()
    }

    data class Params(
        val hashtag: String,
    )
}

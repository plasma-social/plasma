package social.plasma.domain.interactors

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import social.plasma.data.daos.LastRequestDao
import social.plasma.domain.Interactor
import social.plasma.models.HashTag
import social.plasma.models.LastRequestEntity
import social.plasma.models.Request
import social.plasma.nostr.relay.RelayManager
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.Filter
import social.plasma.nostr.relay.message.RelayMessage
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class SyncHashTagEvents @Inject constructor(
    private val relay: RelayManager,
    private val storeEvents: StoreEvents,
    private val lastRequestDao: LastRequestDao,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : Interactor<SyncHashTagEvents.Params>() {

    override suspend fun doWork(params: Params) {
        val hashtagName = params.hashtag.name
        withContext(ioDispatcher) {
            val lastRequest =
                lastRequestDao.lastRequest(Request.SYNC_HASHTAG, hashtagName)?.timestamp
                    ?: Instant.EPOCH

            val subscribeMessage = SubscribeMessage(
                Filter(hashTags = setOf(hashtagName), since = lastRequest, limit = 250),
            )

            val unsubscribeMessage = relay.subscribe(subscribeMessage)
            val subscriptionEvents =
                relay.relayMessages.filterIsInstance<RelayMessage.EventRelayMessage>()
                    .filter { it.subscriptionId == unsubscribeMessage.subscriptionId }
                    .map { it.event }

            storeEvents(subscriptionEvents)
            storeEvents.flow.onStart {
                lastRequestDao.upsert(
                    LastRequestEntity(
                        request = Request.SYNC_HASHTAG,
                        resourceId = hashtagName
                    )
                )
            }.onCompletion {
                relay.unsubscribe(unsubscribeMessage)
            }.collect()
        }
    }

    data class Params(
        val hashtag: HashTag,
    )
}

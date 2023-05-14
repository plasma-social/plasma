package social.plasma.domain.interactors

import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import social.plasma.data.daos.LastRequestDao
import social.plasma.domain.Interactor
import social.plasma.models.LastRequestEntity
import social.plasma.models.Request
import social.plasma.nostr.relay.RelayManager
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.Filter.Companion.userMetaData
import social.plasma.nostr.relay.message.RelayMessage
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

class SyncMetadata @Inject constructor(
    private val relay: RelayManager,
    private val storeMetadataEvents: StoreMetadataEvents,
    private val lastRequestDao: LastRequestDao,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : Interactor<SyncMetadata.Params>() {
    private val ttl = 24.hours.toJavaDuration()

    override suspend fun doWork(params: Params) {
        withContext(ioDispatcher) {
            val lastRequest =
                lastRequestDao.lastRequest(Request.SYNC_METADATA, params.pubKey.key.hex())

            if (lastRequest?.isStillValid(ttl) == true) {
                return@withContext
            }

            val unsubscribeMessage =
                relay.subscribe(SubscribeMessage(userMetaData(params.pubKey.key.hex())))
            val subscriptionMessages = relay.relayMessages
                .filterIsInstance<RelayMessage.EventRelayMessage>()
                .filter { it.subscriptionId == unsubscribeMessage.subscriptionId }
                .take(params.limit)
                .map { it.event }
                .onEach {
                    lastRequestDao.upsert(
                        LastRequestEntity(
                            request = Request.SYNC_METADATA,
                            resourceId = params.pubKey.key.hex(),
                        )
                    )
                }

            storeMetadataEvents(subscriptionMessages)
            storeMetadataEvents.flow.onCompletion {
                relay.unsubscribe(unsubscribeMessage)
            }.collect()
        }
    }

    data class Params(
        val pubKey: PubKey,
        val limit: Int = 3,
    )
}

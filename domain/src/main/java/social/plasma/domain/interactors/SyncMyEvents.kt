package social.plasma.domain.interactors

import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.toByteString
import social.plasma.domain.Interactor
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.Filter
import social.plasma.shared.repositories.api.AccountStateRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class SyncMyEvents @Inject constructor(
    private val relay: Relay,
    private val storeEvents: StoreEvents,
    private val accountStateRepository: AccountStateRepository,
    private val syncMetadata: SyncMetadata,
    private val syncProfileData: SyncProfileData,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : Interactor<Unit>() {

    override suspend fun doWork(params: Unit) = withContext(ioDispatcher) {
        accountStateRepository.isLoggedIn.distinctUntilChanged().flatMapLatest { isLoggedIn ->
            if (isLoggedIn) {
                val pubkey = PubKey(accountStateRepository.getPublicKey()!!.toByteString())

                val subscribeMessage = ClientMessage.SubscribeMessage(
                    Filter(
                        authors = setOf(pubkey.key.hex()),
                        since = Instant.EPOCH,
                        limit = 500,
                    ),
                    Filter(pTags = setOf(pubkey.key.hex()), limit = 500, kinds = setOf(1, 6))
                )

                val subscription =
                    relay.subscribe(subscribeMessage).distinctUntilChanged().map { it.event }

                merge(
                    storeEvents.flow.onStart { storeEvents(subscription) },
                    syncProfileData(SyncProfileData.Params(pubkey)),
                    syncMetadata(SyncMetadata.Params(pubkey)),
                )
            } else emptyFlow()
        }.collect()
    }

    data class Params(
        val pubKey: PubKey,
    )
}

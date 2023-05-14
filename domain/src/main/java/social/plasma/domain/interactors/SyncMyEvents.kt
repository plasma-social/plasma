package social.plasma.domain.interactors

import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.toByteString
import social.plasma.domain.Interactor
import social.plasma.models.Event
import social.plasma.nostr.relay.RelayManager
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.Filter
import social.plasma.nostr.relay.message.RelayMessage
import social.plasma.shared.repositories.api.AccountStateRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class SyncMyEvents @Inject constructor(
    private val relay: RelayManager,
    private val storeEvents: StoreEvents,
    private val accountStateRepository: AccountStateRepository,
    private val syncMetadata: SyncMetadata,
    private val storeContactList: StoreContactList,
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
                    Filter(
                        authors = setOf(pubkey.key.hex()),
                        since = Instant.EPOCH,
                        limit = 1,
                        kinds = setOf(Event.Kind.ContactList)
                    ),
                    Filter(pTags = setOf(pubkey.key.hex()), limit = 500, kinds = setOf(1, 6))
                )

                val unsubscribeMessage = relay.subscribe(subscribeMessage)
                val subscriptionEvents =
                    relay.relayMessages.filterIsInstance<RelayMessage.EventRelayMessage>()
                        .filter { it.subscriptionId == unsubscribeMessage.subscriptionId }
                        .distinctUntilChanged()
                        .map { it.event }
                        .onCompletion { relay.unsubscribe(unsubscribeMessage) }

                merge(
                    storeEvents.flow.onStart { storeEvents(subscriptionEvents) },
                    storeContactList.flow.onStart { storeContactList(subscriptionEvents) },
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

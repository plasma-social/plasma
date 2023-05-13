package social.plasma.domain.interactors

import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import social.plasma.domain.Interactor
import social.plasma.nostr.relay.RelayManager
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.Filter
import social.plasma.nostr.relay.message.RelayMessage
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class SyncProfileData @Inject constructor(
    private val relay: RelayManager,
    private val storeMetadataEvents: StoreMetadataEvents,
    private val storeEvents: StoreEvents,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : Interactor<SyncProfileData.Params>() {

    override suspend fun doWork(params: Params) = withContext(ioDispatcher) {
        val pubkey = params.pubKey

        val subscribeMessage = ClientMessage.SubscribeMessage(
            Filter.contactList(pubkey.key.hex()),
            Filter.userMetaData(pubkey.key.hex()),
            Filter.userNotes(pubkey.key.hex()),
        )

        val unsubscribeMessage = relay.subscribe(subscribeMessage)
        val subscriptionMessages = relay.relayMessages
            .filterIsInstance<RelayMessage.EventRelayMessage>()
            .filter { it.subscriptionId == unsubscribeMessage.subscriptionId }
            .distinctUntilChanged()
            .map { it.event }


        merge(
            storeMetadataEvents.flow.onStart { storeMetadataEvents(subscriptionMessages) },
            storeEvents.flow.onStart { storeEvents(subscriptionMessages) },
        ).onCompletion {
            relay.unsubscribe(unsubscribeMessage)
        }.collect()
    }

    data class Params(
        val pubKey: PubKey,
    )
}

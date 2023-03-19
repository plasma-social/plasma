package social.plasma.domain.interactors

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import social.plasma.domain.Interactor
import social.plasma.models.PubKey
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.Filter
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class SyncProfileData @Inject constructor(
    private val relay: Relay,
    private val storeContactList: StoreContactList,
    private val storeMetadataEvents: StoreMetadataEvents,
    private val storeEvents: StoreEvents,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : Interactor<SyncProfileData.Params>() {

    override suspend fun doWork(params: Params) = withContext(ioDispatcher) {
        val pubkey = params.pubKey

        val subscribeMessage = ClientMessage.SubscribeMessage(
            Filter.contactList(pubkey.hex),
            Filter.userMetaData(pubkey.hex),
            Filter.userNotes(pubkey.hex),
        )

        val subscription = relay.subscribe(subscribeMessage)
            .distinctUntilChanged()
            .map { it.event }

        merge(
            storeContactList.flow.onStart { storeContactList(subscription) },
            storeMetadataEvents.flow.onStart { storeMetadataEvents(subscription) },
            storeEvents.flow.onStart { storeEvents(subscription) },
        ).collect()
    }

    data class Params(
        val pubKey: PubKey,
    )
}
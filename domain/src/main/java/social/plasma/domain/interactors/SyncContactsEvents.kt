package social.plasma.domain.interactors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import social.plasma.domain.InvokeStatus
import social.plasma.domain.SubjectInteractor
import social.plasma.domain.observers.ObserveContacts
import social.plasma.models.PubKey
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.Filter
import social.plasma.nostr.relay.message.RelayMessage.EventRelayMessage
import social.plasma.shared.repositories.api.AccountStateRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class SyncContactsEvents @Inject constructor(
    private val relay: Relay,
    private val accountStateRepository: AccountStateRepository,
    private val storeEvents: StoreEvents,
    private val observeContacts: ObserveContacts,
) : SubjectInteractor<Unit, Any>() {

    override fun createObservable(params: Unit): Flow<Any> {
        val pubKey = PubKey.of(accountStateRepository.getPublicKey()!!)

        return observeContacts.flow.onStart {
            observeContacts(ObserveContacts.Params(pubKey))
        }.filter { it.isNotEmpty() }.flatMapLatest { contactList ->
            val contacts = contactList.map { it.pubKey }.toSet()
            storeEvents(
                relay.subscribe(
                    SubscribeMessage(
                        filter = Filter.userNotes(
                            pubKeys = contacts
                        )
                    )
                ).map { it.event }
            )
            storeEvents.flow
        }
    }
}
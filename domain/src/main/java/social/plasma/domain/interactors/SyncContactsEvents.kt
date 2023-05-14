package social.plasma.domain.interactors

import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import okio.ByteString.Companion.toByteString
import social.plasma.domain.SubjectInteractor
import social.plasma.domain.observers.ObserveContacts
import social.plasma.nostr.relay.RelayManager
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.Filter
import social.plasma.nostr.relay.message.RelayMessage
import social.plasma.shared.repositories.api.AccountStateRepository
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class SyncContactsEvents @Inject constructor(
    private val relay: RelayManager,
    private val accountStateRepository: AccountStateRepository,
    private val storeEvents: StoreEvents,
    private val observeContacts: ObserveContacts,
) : SubjectInteractor<Unit, Any>() {

    override fun createObservable(params: Unit): Flow<Any> {
        val pubKey = PubKey(accountStateRepository.getPublicKey()?.toByteString()!!)

        return observeContacts.flow.onStart {
            observeContacts(ObserveContacts.Params(pubKey))
        }.filter { it.isNotEmpty() }.flatMapLatest { contactList ->
            val contacts = contactList.map { it.pubKey }.toSet()
            val unsubscribeMessage = relay.subscribe(
                SubscribeMessage(
                    filter = Filter.userNotes(
                        pubKeys = contacts
                    )
                )
            )
            storeEvents(
                relay.relayMessages.filterIsInstance<RelayMessage.EventRelayMessage>()
                    .filter { it.subscriptionId == unsubscribeMessage.subscriptionId }
                    .map { it.event }
            )
            storeEvents.flow.onCompletion {
                relay.unsubscribe(unsubscribeMessage)
            }
        }
    }
}

package social.plasma.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import social.plasma.data.daos.ContactsDao
import social.plasma.data.daos.RelayInfoDao
import social.plasma.domain.SubjectInteractor
import social.plasma.models.Contact
import social.plasma.models.ContactEntity
import social.plasma.models.Event
import social.plasma.models.RelayEntity
import social.plasma.nostr.models.RelayDetails
import social.plasma.nostr.relay.message.EventRefiner
import java.time.Instant
import javax.inject.Inject

class StoreContactList @Inject constructor(
    private val contactListDao: ContactsDao,
    private val relayInfoDao: RelayInfoDao,
    private val eventRefiner: EventRefiner,
) : SubjectInteractor<Flow<Event>, Any>() {

    override fun createObservable(params: Flow<Event>): Flow<Any> {
        val contactListEvent = params.filter { it.kind == Event.Kind.ContactList }
            .filter { newEvent ->
                val dbNewestEvent = contactListDao.getNewestEvent(newEvent.pubKey.hex())
                dbNewestEvent == null || dbNewestEvent.createdAt <= newEvent.createdAt.epochSecond
            }
            .distinctUntilChanged()

        val relayDataFlow = contactListEvent.map {
            eventRefiner.toRelayDetailList(it)
        }
            .filterNotNull()
            .map {
                it.pubKey to it.content
            }.onEach { (pubkey, relays) ->
                relayInfoDao.replace(relays.map { it.toRelayEntity(pubkey.hex()) })
            }

        val contactsFlow = contactListEvent
            .map { it.typed(it.typed("").contacts()) }
            .filterNotNull()
            .map {
                it.pubKey.hex() to it.contacts()
            }
            .onEach { (owner, contacts) ->
                contactListDao.replace(contacts.map { it.toContactEntity(owner) })
            }

        return merge(contactsFlow, relayDataFlow)
    }
}

private fun Map.Entry<String, RelayDetails>.toRelayEntity(
    pubkey: String,
): RelayEntity {
    return RelayEntity(
        url = key,
        read = value.read,
        write = value.write,
        pubkey = pubkey
    )
}

private fun Contact.toContactEntity(owner: String) = ContactEntity(
    owner = owner,
    pubKey = pubKey.hex(),
    petName = petName,
    homeRelay = homeRelayUrl,
    createdAt = Instant.now().epochSecond,
)

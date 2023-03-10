package social.plasma.repository

import kotlinx.coroutines.flow.*
import okio.ByteString.Companion.decodeHex
import social.plasma.db.contacts.ContactEntity
import social.plasma.db.contacts.ContactsDao
import social.plasma.nostr.models.Contact
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.EventRefiner
import social.plasma.nostr.relay.message.Filter
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

interface ContactListRepository {
    fun observeContactLists(pubkey: String): Flow<Set<Contact>>

    fun observeFollowState(pubKey: String, contactPubKey: String): Flow<Boolean>

    fun observeFollowingCount(pubkey: String): Flow<Long>

    fun observeFollowersCount(pubkey: String): Flow<Int>

    fun syncContactList(pubkey: String): Flow<Set<Contact>>
}

class RealContactListRepository @Inject constructor(
    private val relay: Relay,
    private val eventRefiner: EventRefiner,
    private val contactListDao: ContactsDao,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : ContactListRepository {

    override fun observeContactLists(pubkey: String): Flow<Set<Contact>> {
        return contactListDao.observeContacts(pubkey)
            .distinctUntilChanged()
            .map { list -> list.map { it.toContact() } }
            .map { it.toSet() }
    }

    override fun observeFollowState(pubKey: String, contactPubKey: String): Flow<Boolean> {
        return contactListDao.observeOwnerFollowsContact(pubKey, contactPubKey)
    }

    override fun observeFollowingCount(pubkey: String): Flow<Long> {
        return contactListDao.observeFollowingCount(pubkey)
    }

    override fun observeFollowersCount(pubkey: String): Flow<Int> =
        relay.subscribe(SubscribeMessage(Filter.userFollowers(pubkey)))
            .distinctUntilChanged()
            .runningFold(
                emptySet<String>()
            ) { acc, value ->
                acc + value.event.pubKey.hex()
            }
            .map { it.size }

    override fun syncContactList(pubkey: String): Flow<Set<Contact>> {
        return relay.subscribe(SubscribeMessage(Filter.contactList(pubkey)))
            .distinctUntilChanged()
            .map { eventRefiner.toContacts(it) }
            .filterNotNull()
            .distinctUntilChanged()
            .map {
                it.pubKey.hex() to it.contacts()
            }
            .map { (owner, contacts) ->
                contactListDao.insert(contacts.map { it.toContactEntity(owner) })
                contacts
            }
            .flowOn(ioDispatcher)
    }
}

private fun ContactEntity.toContact() = Contact(
    pubKey = pubKey.decodeHex(),
    homeRelayUrl = homeRelay,
    petName = petName,
)

private fun Contact.toContactEntity(owner: String) = ContactEntity(
    owner = owner,
    pubKey = pubKey.hex(),
    petName = petName,
    homeRelay = homeRelayUrl,
    createdAt = Instant.now().epochSecond,
)

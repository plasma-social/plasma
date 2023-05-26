package social.plasma.shared.repositories.real

import app.cash.nostrino.crypto.SecKey
import okio.ByteString.Companion.toByteString
import social.plasma.data.daos.ContactsDao
import social.plasma.models.Event
import social.plasma.models.events.EventEntity
import social.plasma.nostr.relay.RelayManager
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.repositories.api.ContactsRepository
import java.time.Instant
import javax.inject.Inject

class RealContactsRepository @Inject constructor(
    private val contactsDao: ContactsDao,
    private val relayManager: RelayManager,
    private val accountStateRepository: AccountStateRepository,
) : ContactsRepository {
    override suspend fun follow(pubKeyHex: String) {
        val currentContactList = getCurrentContactList()
        requireNotNull(currentContactList)

        val tags = currentContactList.tags + setOf(listOf("p", pubKeyHex))

        updateContactListTags(tags, currentContactList)
    }

    override suspend fun unfollow(pubKeyHex: String) {
        val currentContactList = getCurrentContactList()
        requireNotNull(currentContactList)

        val tags = currentContactList.tags - setOf(listOf("p", pubKeyHex))

        if (tags != currentContactList.tags) {
            updateContactListTags(tags, currentContactList)
        }
    }

    private suspend fun getCurrentContactList(): EventEntity? {
        val myPubkey = accountStateRepository.getPublicKey()?.toByteString()
            ?: throw IllegalStateException("Public key required to follow")

        return contactsDao.getContactListEvent(myPubkey.hex())
    }

    private suspend fun updateContactListTags(
        tags: List<List<String>>,
        currentContactList: EventEntity,
    ) {
        val secKey = SecKey(
            accountStateRepository.getSecretKey()?.toByteString()
                ?: throw IllegalStateException("Secret key required to update contact list")
        )
        relayManager.send(
            ClientMessage.EventMessage(
                Event.createEvent(
                    pubKey = secKey.pubKey.key,
                    secretKey = secKey.key,
                    createdAt = Instant.now(),
                    kind = Event.Kind.ContactList,
                    tags = tags,
                    content = currentContactList.content
                )
            )
        )
    }
}

package social.plasma.shared.repositories.real

import app.cash.nostrino.crypto.SecKeyGenerator
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import social.plasma.data.daos.fakes.FakeContactsDao
import social.plasma.data.nostr.fakes.FakeRelayManager
import social.plasma.models.Event
import social.plasma.models.events.EventEntity
import social.plasma.shared.repositories.fakes.FakeAccountStateRepository

class RealContactsRepositoryTest {
    private val relayManager = FakeRelayManager()
    private val accountStateRepository = FakeAccountStateRepository()
    private val contactsDao = FakeContactsDao()
    private val secKey = SecKeyGenerator().generate()
    private val myPubkeyHex = secKey.pubKey.key.hex()

    @Before
    fun setUp() {
        accountStateRepository.setSecretKey(secKey.key.toByteArray())
        accountStateRepository.setPublicKey(secKey.pubKey.key.toByteArray())
    }

    @Test
    fun `follow should send a contact list event with the new contact`() = runTest {
        contactsDao.getContactListEventResponses.add(createContactEventEntity())

        createRepository().followPubkey("pubkey2")

        with(relayManager.sendEventTurbine.awaitItem().event) {
            assertThat(kind).isEqualTo(Event.Kind.ContactList)
            assertThat(tags).containsExactly(listOf("p", "pubkey"), listOf("p", "pubkey2"))
            assertThat(pubKey).isEqualTo(secKey.pubKey.key)
            assertThat(content).isEqualTo("test")
        }

        assertThat(contactsDao.getContactListEventRequests.awaitItem()).isEqualTo(myPubkeyHex)
    }


    @Test
    fun `unfollow should send a contact list event excluding the unfollowed contact`() = runTest {
        contactsDao.getContactListEventResponses.add(
            createContactEventEntity(
                tags = listOf(
                    listOf("p", "pubkey"),
                    listOf("p", "pubkey2")
                )
            )
        )

        createRepository().unfollowPubkey("pubkey")

        with(relayManager.sendEventTurbine.awaitItem().event) {
            assertThat(kind).isEqualTo(Event.Kind.ContactList)
            assertThat(tags).containsExactly(listOf("p", "pubkey2"))
            assertThat(pubKey).isEqualTo(secKey.pubKey.key)
            assertThat(content).isEqualTo("test")
        }

        assertThat(contactsDao.getContactListEventRequests.awaitItem()).isEqualTo(myPubkeyHex)
    }

    @Test
    fun `unfollowing a pubkey not in the contact list`() = runTest {
        contactsDao.getContactListEventResponses.add(
            createContactEventEntity(
                tags = listOf(
                    listOf("p", "pubkey2")
                )
            )
        )

        createRepository().unfollowPubkey("pubkey")

        relayManager.sendEventTurbine.expectNoEvents()
    }

    @Test
    fun `following a hashtag should send a contact list event with the new hashtag`() = runTest {
        contactsDao.getContactListEventResponses.add(createContactEventEntity())

        createRepository().followHashTag("hashtag")

        with(relayManager.sendEventTurbine.awaitItem().event) {
            assertThat(kind).isEqualTo(Event.Kind.ContactList)
            assertThat(tags).containsExactly(listOf("p", "pubkey"), listOf("t", "hashtag"))
            assertThat(pubKey).isEqualTo(secKey.pubKey.key)
            assertThat(content).isEqualTo("test")
        }

        assertThat(contactsDao.getContactListEventRequests.awaitItem()).isEqualTo(myPubkeyHex)
    }

    @Test
    fun `unfollowing a hashtag should send a contact list event excluding the unfollowed hashtag`() =
        runTest {
            contactsDao.getContactListEventResponses.add(
                createContactEventEntity(
                    tags = listOf(
                        listOf("p", "pubkey"),
                        listOf("t", "hashtag")
                    )
                )
            )

            createRepository().unfollowHashTag("hashtag")

            with(relayManager.sendEventTurbine.awaitItem().event) {
                assertThat(kind).isEqualTo(Event.Kind.ContactList)
                assertThat(tags).containsExactly(listOf("p", "pubkey"))
                assertThat(pubKey).isEqualTo(secKey.pubKey.key)
                assertThat(content).isEqualTo("test")
            }

            assertThat(contactsDao.getContactListEventRequests.awaitItem()).isEqualTo(myPubkeyHex)
        }

    @Test
    fun `unfollowing a hashtag not in the contact list`() = runTest {
        contactsDao.getContactListEventResponses.add(
            createContactEventEntity(
                tags = listOf(
                    listOf("p", "pubkey")
                )
            )
        )

        createRepository().unfollowHashTag("hashtag")

        relayManager.sendEventTurbine.expectNoEvents()
    }


    private fun createContactEventEntity(
        tags: List<List<String>> = listOf(listOf("p", "pubkey")),
    ) = EventEntity(
        id = "1",
        kind = Event.Kind.ContactList,
        tags = tags,
        pubkey = myPubkeyHex,
        content = "test",
        sig = "",
        createdAt = 0,
    )

    private fun createRepository(): RealContactsRepository {
        return RealContactsRepository(
            contactsDao = contactsDao,
            relayManager = relayManager,
            accountStateRepository = accountStateRepository,
        )
    }
}

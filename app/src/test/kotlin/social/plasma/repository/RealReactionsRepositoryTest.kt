package social.plasma.repository

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import social.plasma.db.ext.toEventEntity
import social.plasma.models.crypto.KeyGenerator
import social.plasma.nostr.models.Event
import social.plasma.nostr.relay.message.NostrMessageAdapter
import social.plasma.prefs.FakePreference
import java.time.Instant


@OptIn(ExperimentalCoroutinesApi::class)
internal class RealReactionsRepositoryTest {
    private val keys = KeyGenerator().generateKeyPair()
    private val mySecretKey = keys.sec.toByteArray()
    private val myPubKey = keys.pub.toByteArray()

    private val relay = FakeRelay()
    private val testDispatcher = StandardTestDispatcher()
    private val eventsDao = FakeEventsDao()
    private val mySecretKeyPref = FakePreference(mySecretKey)
    private val moshi =
        Moshi.Builder().add(NostrMessageAdapter()).addLast(KotlinJsonAdapterFactory()).build()

    private val repo: RealReactionsRepository
        get() {
            return RealReactionsRepository(
                myPubkey = FakePreference(myPubKey),
                mySecretKey = mySecretKeyPref,
                relays = relay,
                ioDispatcher = testDispatcher,
                eventsDao = eventsDao,
                moshi = moshi,
            )
        }

    @BeforeEach
    fun setup() {
        mySecretKeyPref.value = mySecretKey
    }

    @Test
    fun `reacting to a note without tags`() = runTest(testDispatcher) {
        eventsDao.eventsByIdTurbine.add(createEvent().toEventEntity())

        repo.sendReaction(NOTE_HEX)

        with(relay.sendEventTurbine.awaitItem().event) {
            assertThat(kind).isEqualTo(Event.Kind.Reaction)
            assertThat(pubKey).isEqualTo(myPubKey.toByteString())
            assertThat(tags).containsExactly(
                listOf("e", NOTE_HEX),
                listOf("p", NPUB_HEX),
            ).inOrder()
        }

        relay.sendEventTurbine.expectNoEvents()
    }

    @Test
    fun `reacting to a note with tags`() = runTest(testDispatcher) {
        eventsDao.eventsByIdTurbine.add(
            createEvent(
                tags = listOf(
                    listOf("e", "test"),
                    listOf("p", "test"),
                    listOf("p", "test2")
                )
            ).toEventEntity()
        )

        repo.sendReaction(NOTE_HEX)

        with(relay.sendEventTurbine.awaitItem().event) {
            assertThat(tags).containsExactly(
                listOf("e", "test"),
                listOf("p", "test"),
                listOf("p", "test2"),
                listOf("e", NOTE_HEX),
                listOf("p", NPUB_HEX),
            ).inOrder()
        }
    }

    @Test
    fun `reacting without a secret key doesnt submit a reaction`() = runTest(testDispatcher) {
        mySecretKeyPref.value = null

        repo.sendReaction(NOTE_HEX)

        relay.sendEventTurbine.expectNoEvents()
    }

    @Test
    fun `reacting to a note that doesn't exist in the dao`() = runTest(testDispatcher) {
        eventsDao.eventsByIdTurbine.add(null)

        repo.sendReaction(NOTE_HEX)

        relay.sendEventTurbine.expectNoEvents()
    }

    @Test
    fun `reposting a note`() = runTest(testDispatcher) {
        val note = createEvent()
        val noteJsonString = moshi.adapter(Event::class.java).toJson(note)
        eventsDao.eventsByIdTurbine.add(note.toEventEntity())

        repo.repost(NOTE_HEX)

        with(relay.sendEventTurbine.awaitItem().event) {
            assertThat(kind).isEqualTo(Event.Kind.Repost)
            assertThat(content).isEqualTo(noteJsonString)
            assertThat(tags).containsExactly(
                listOf("e", NOTE_HEX, "", "root"),
                listOf("p", NPUB_HEX),
            ).inOrder()
        }
    }

    @Test
    fun `reposting a note with tags`() = runTest(testDispatcher) {
        val note = createEvent(
            tags = listOf(
                listOf("e", "test"),
                listOf("p", "test"),
                listOf("p", "test2")
            )
        )
        val noteJsonString = moshi.adapter(Event::class.java).toJson(note)
        eventsDao.eventsByIdTurbine.add(note.toEventEntity())

        repo.repost(NOTE_HEX)

        with(relay.sendEventTurbine.awaitItem().event) {
            assertThat(kind).isEqualTo(Event.Kind.Repost)
            assertThat(content).isEqualTo(noteJsonString)
            assertThat(tags).containsExactly(
                listOf("e", "test"),
                listOf("p", "test"),
                listOf("p", "test2"),
                listOf("e", NOTE_HEX, "", "root"),
                listOf("p", NPUB_HEX),
            ).inOrder()
        }
    }

    @Test
    fun `reposting without a secret key doesnt submit an event`() = runTest(testDispatcher) {
        mySecretKeyPref.value = null

        repo.repost(NOTE_HEX)

        relay.sendEventTurbine.expectNoEvents()
    }

    @Test
    fun `reposting a note that doesn't exist in the dao`() = runTest(testDispatcher) {
        eventsDao.eventsByIdTurbine.add(null)

        repo.repost(NOTE_HEX)

        relay.sendEventTurbine.expectNoEvents()
    }

    private fun createEvent(
        pubKey: String = NPUB_HEX,
        tags: List<List<String>> = emptyList(),
    ) = Event.createEvent(
        pubKey = pubKey.decodeHex(),
        tags = tags,
        content = "test",
        createdAt = Instant.now(),
        secretKey = mySecretKey.toByteString(),
        kind = 1,
    )

    companion object {
        private const val NOTE_HEX =
            "69dac2a7c46835a5c197eab632d262b84b8a017a1226739f08f163b6fede8c74"

        private const val NPUB_HEX =
            "12bbde125d610b64f79194eb80478fe33ad95ed34184c7f0577e6214f3266cb0"

    }
}

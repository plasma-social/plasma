package social.plasma.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import social.plasma.crypto.KeyGenerator
import social.plasma.db.notes.FakeNoteDao
import social.plasma.db.notes.NoteView
import social.plasma.db.notes.NoteWithUser
import social.plasma.nostr.models.Event
import social.plasma.prefs.FakePreference
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
internal class RealReactionsRepositoryTest {
    private val keys = KeyGenerator().generateKeyPair()
    private val relay = FakeRelay()
    private val testDispatcher = StandardTestDispatcher()
    private val noteDao = FakeNoteDao()
    private val mySecretKeyPref = FakePreference(keys.sec.toByteArray())
    private val myPubKey = keys.pub.toByteArray()


    private val repo: RealReactionsRepository
        get() {
            return RealReactionsRepository(
                myPubkey = FakePreference(myPubKey),
                mySecretKey = mySecretKeyPref,
                relays = relay,
                ioDispatcher = testDispatcher,
                noteDao = noteDao,
            )
        }

    @BeforeEach
    fun setup() {
        mySecretKeyPref.value = keys.sec.toByteArray()
    }

    @Test
    fun `reacting to a note without tags`() = runTest(testDispatcher) {
        noteDao.noteWithUserTurbine.add(createNote())

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
        noteDao.noteWithUserTurbine.add(
            createNote(
                tags = listOf(
                    listOf("e", "test"),
                    listOf("p", "test"),
                    listOf("p", "test2")
                )
            )
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
        noteDao.noteWithUserTurbine.add(null)

        repo.sendReaction(NOTE_HEX)

        relay.sendEventTurbine.expectNoEvents()
    }


    private fun createNote(
        id: String = NOTE_HEX,
        pubKey: String = NPUB_HEX,
        isReply: Boolean = false,
        tags: List<List<String>> = emptyList(),
    ) = NoteWithUser(
        userMetadataEntity = null, noteEntity = NoteView(
            id = id,
            "",
            Instant.now().epochSecond,
            isReply = isReply,
            pubkey = pubKey,
            tags = tags,
            reactionCount = 0,
        )
    )

    companion object {
        private const val NOTE_HEX =
            "69dac2a7c46835a5c197eab632d262b84b8a017a1226739f08f163b6fede8c74"

        private const val NPUB_HEX =
            "12bbde125d610b64f79194eb80478fe33ad95ed34184c7f0577e6214f3266cb0"

    }
}

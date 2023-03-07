package social.plasma.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import social.plasma.crypto.KeyGenerator
import social.plasma.db.notes.FakeNoteDao
import social.plasma.db.notes.NoteView
import social.plasma.db.notes.NoteWithUser
import social.plasma.prefs.FakePreference
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
internal class RealNoteRepositoryTest {
    private val keys = KeyGenerator().generateKeyPair()
    private val relay = FakeRelay()
    private val testDispatcher = StandardTestDispatcher()
    private val noteDao = FakeNoteDao()

    private val repo = RealNoteRepository(
        noteDao = noteDao,
        contactListRepository = FakeContactListRepo(),
        myPubKey = FakePreference(keys.pub.toByteArray()),
        mySecretKey = FakePreference(keys.sec.toByteArray()),
        relay = relay,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `post new note without mentions`() = runTest(testDispatcher) {
        repo.postNote("Test")

        with(relay.sendNoteTurbine.awaitItem()) {
            assertThat(tags).isEmpty()
            assertThat(text).isEqualTo("Test")
        }

        relay.sendNoteTurbine.expectNoEvents()
    }

    @Test
    fun `post new note with npub and note mentions`() = runTest(testDispatcher) {
        repo.postNote("Test tagging $NPUB, with event $NOTE_BECH")

        with(relay.sendNoteTurbine.awaitItem()) {
            assertThat(tags).containsExactly(
                listOf("e", NOTE_HEX),
                listOf("p", NPUB_HEX),
            ).inOrder()
            assertThat(text).isEqualTo("Test tagging #[1], with event #[0]")
        }

        relay.sendNoteTurbine.expectNoEvents()
    }

    @Test
    fun `post new note mentioning the same npub multiple times`() = runTest(testDispatcher) {
        repo.postNote("Thanks to @$NPUB, for being @$NPUB")

        with(relay.sendNoteTurbine.awaitItem()) {
            assertThat(tags).containsExactly(
                listOf("p", NPUB_HEX),
            )
            assertThat(text).isEqualTo("Thanks to #[0], for being #[0]")
        }

        relay.sendNoteTurbine.expectNoEvents()
    }

    @Test
    fun `post new note mentioning the same event multiple times`() = runTest(testDispatcher) {
        repo.postNote("Thanks to @$NOTE_BECH, for being @$NOTE_BECH")

        with(relay.sendNoteTurbine.awaitItem()) {
            assertThat(tags).containsExactly(
                listOf("e", NOTE_HEX)
            )
            assertThat(text).isEqualTo("Thanks to #[0], for being #[0]")
        }

        relay.sendNoteTurbine.expectNoEvents()
    }

    @Test
    fun `reply to root note that doesn't contain mentions`() = runTest(testDispatcher) {
        noteDao.noteWithUserTurbine.add(createNote())

        repo.replyToNote(NOTE_HEX, "test")

        with(relay.sendNoteTurbine.awaitItem()) {
            assertThat(tags).containsExactly(
                listOf("e", NOTE_HEX),
                listOf("p", NPUB_HEX),
            ).inOrder()

            assertThat(text).isEqualTo("test")
        }

        relay.sendNoteTurbine.expectNoEvents()
    }

    @Test
    fun `reply to root note that contains mentions`() = runTest(testDispatcher) {
        noteDao.noteWithUserTurbine.add(
            createNote(
                tags = listOf(
                    listOf("e", "orginaleventid"),
                    listOf("p", "originalpubkey"),
                )
            )
        )

        repo.replyToNote(NOTE_HEX, "replying to another reply @$NPUB, @$NOTE_BECH")

        with(relay.sendNoteTurbine.awaitItem()) {
            assertThat(tags).containsExactly(
                listOf("e", "orginaleventid"),
                listOf("e", NOTE_HEX),
                listOf("p", "originalpubkey"),
                listOf("p", NPUB_HEX),
            ).inOrder()

            assertThat(text).isEqualTo("replying to another reply #[3], #[1]")
        }

        relay.sendNoteTurbine.expectNoEvents()
    }

    @Test
    fun `reply note that contains mentions`() = runTest(testDispatcher) {
        noteDao.noteWithUserTurbine.add(
            createNote(
                tags = listOf(
                    listOf("e", "rootid"),
                    listOf("e", "mentionid"),
                    listOf("e", "replyid"),
                    listOf("p", "somenpub"),
                    listOf("p", "someothernpub"),
                ),
                isReply = true,
            )
        )

        repo.replyToNote(NOTE_HEX, "replying to another reply @$NPUB, @$NOTE_BECH")

        with(relay.sendNoteTurbine.awaitItem()) {
            assertThat(tags).containsExactly(
                listOf("e", "rootid"),
                listOf("e", NOTE_HEX),
                listOf("p", "somenpub"),
                listOf("p", "someothernpub"),
                listOf("p", NPUB_HEX),
            ).inOrder()

            assertThat(text).isEqualTo("replying to another reply #[4], #[1]")
        }

        relay.sendNoteTurbine.expectNoEvents()
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

        private const val NPUB = "npub1z2aauyjavy9kfau3jn4cq3u0uvadjhkngxzv0uzh0e3pfuexdjcql0pyy7"

        private const val NOTE_BECH =
            "note1d8dv9f7ydq66tsvha2mr95nzhp9c5qt6zgn888cg793mdlk7336q0ha930"
    }
}

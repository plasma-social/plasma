package social.plasma.domain.interactors

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.toByteString
import org.junit.After
import org.junit.Before
import org.junit.Test
import social.plasma.domain.InvokeError
import social.plasma.domain.InvokeStarted
import social.plasma.domain.InvokeStatus
import social.plasma.domain.InvokeSuccess
import social.plasma.models.Event
import social.plasma.models.NoteId
import social.plasma.models.NoteView
import social.plasma.models.NoteWithUser
import app.cash.nostrino.crypto.PubKey
import okio.ByteString.Companion.decodeHex
import social.plasma.models.crypto.KeyGenerator
import social.plasma.shared.repositories.fakes.FakeAccountStateRepository
import social.plasma.shared.repositories.fakes.FakeNoteRepository
import java.time.Instant


@OptIn(ExperimentalCoroutinesApi::class)
class SendNoteReactionTest {
    private val keys = KeyGenerator().generateKeyPair()
    private val mySecretKey = keys.sec.toByteArray()
    private val myPubKey = keys.pub.toByteArray()

    private val noteRepository = FakeNoteRepository()
    private val accountStateRepository = FakeAccountStateRepository(
        secretKey = mySecretKey,
        publicKey = myPubKey,
    )
    private val relay = FakeRelay()

    private val TestScope.sendReaction: SendNoteReaction
        get() {
            return SendNoteReaction(
                ioDispatcher = coroutineContext,
                accountStateRepository = accountStateRepository,
                noteRepository = noteRepository,
                relay = relay,
            )
        }

    @Before
    fun setup() {
        accountStateRepository.setSecretKey(mySecretKey)
        accountStateRepository.setPublicKey(myPubKey)
    }

    @After
    fun tearDown() {
        noteRepository.sendNoteEvents.expectNoEvents()
        relay.sendEventTurbine.expectNoEvents()
    }

    @Test
    fun `reacting to a note without tags`() = runTest {
        noteRepository.noteByIdResponse.value = createNote()

        sendReaction(SendNoteReaction.Params(noteId = NOTE_ID)).test {
            assertSuccess()
            with(relay.sendEventTurbine.awaitItem().event) {
                assertThat(kind).isEqualTo(Event.Kind.Reaction)
                assertThat(pubKey).isEqualTo(myPubKey.toByteString())
                assertThat(tags).containsExactly(
                    listOf("e", NOTE_ID.hex),
                    listOf("p", PUBKEY.key.hex()),
                ).inOrder()
            }

        }
    }

    @Test
    fun `reacting to a note with tags`() = runTest {
        noteRepository.noteByIdResponse.value = createNote(
            tags = listOf(
                listOf("e", "test"),
                listOf("p", "test"),
                listOf("p", "test2")
            )
        )

        sendReaction(SendNoteReaction.Params(noteId = NOTE_ID)).test {

            assertSuccess()
            with(relay.sendEventTurbine.awaitItem().event) {
                assertThat(tags).containsExactly(
                    listOf("e", "test"),
                    listOf("p", "test"),
                    listOf("p", "test2"),
                    listOf("e", NOTE_ID.hex),
                    listOf("p", PUBKEY.key.hex()),
                ).inOrder()
            }

        }
    }

    @Test
    fun `reacting without a secret key doesnt submit a reaction`() = runTest {
        accountStateRepository.clearKeys()

        sendReaction(SendNoteReaction.Params(NOTE_ID)).test {
            assertError()
        }

        relay.sendEventTurbine.expectNoEvents()
    }



    @Test
    fun `reacting to a note that doesn't exist in the dao`() = runTest {
        sendReaction(SendNoteReaction.Params(NOTE_ID)).test {
            assertError()
        }
    }


    private fun createNote(
        id: String = NOTE_ID.hex,
        pubKey: String = PUBKEY.key.hex(),
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
            kind = 1,
        )
    )

    private suspend fun ReceiveTurbine<InvokeStatus>.assertSuccess() {
        assertThat(awaitItem()).isEqualTo(InvokeStarted)
        assertThat(awaitItem()).isEqualTo(InvokeSuccess)
        awaitComplete()
    }

    private suspend fun ReceiveTurbine<InvokeStatus>.assertError() {
        assertThat(awaitItem()).isEqualTo(InvokeStarted)
        assertThat(awaitItem()).isInstanceOf(InvokeError::class.java)
        awaitComplete()
    }

    companion object {
        private val NOTE_ID =
            NoteId("69dac2a7c46835a5c197eab632d262b84b8a017a1226739f08f163b6fede8c74")
        private val PUBKEY =
            PubKey("12bbde125d610b64f79194eb80478fe33ad95ed34184c7f0577e6214f3266cb0".decodeHex())

    }
}
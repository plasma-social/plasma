package social.plasma.domain.interactors

import app.cash.nostrino.crypto.PubKey
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeHex
import org.junit.After
import org.junit.Test
import social.plasma.domain.InvokeStatus
import social.plasma.domain.InvokeSuccess
import social.plasma.models.EventTag
import social.plasma.models.HashTag
import social.plasma.models.NoteId
import social.plasma.models.NoteView
import social.plasma.models.NoteWithUser
import social.plasma.models.PubKeyTag
import social.plasma.shared.repositories.fakes.FakeNoteRepository
import java.time.Instant


@OptIn(ExperimentalCoroutinesApi::class)
class SendNoteTest {
    private val noteRepository = FakeNoteRepository()

    private val TestScope.sendNote: SendNote
        get() = SendNote(
            ioDispatcher = coroutineContext,
            noteRepository = noteRepository
        )

    @After
    fun tearDown() {
        noteRepository.sendNoteEvents.expectNoEvents()
    }

    @Test
    fun `post new note without mentions`() = runTest {
        sendNote(SendNote.Params("Test")).test {
            assertSuccess()

            with(noteRepository.sendNoteEvents.awaitItem()) {
                assertThat(tags).isEmpty()
                assertThat(content).isEqualTo("Test")
            }
        }
    }

    @Test
    fun `post new note with npub and note mentions`() = runTest {
        sendNote(SendNote.Params("Test tagging $NPUB, with event $NOTE_BECH")).test {
            assertSuccess()

            with(noteRepository.sendNoteEvents.awaitItem()) {
                assertThat(tags).containsExactly(
                    EventTag(NOTE_ID),
                    PubKeyTag(PUBKEY),
                ).inOrder()

                assertThat(content)
                    .isEqualTo("Test tagging #[1], with event #[0]")
            }
        }
    }

    @Test
    fun `post new note mentioning the same npub multiple times`() = runTest {
        sendNote(SendNote.Params("Thanks to @$NPUB, for being @$NPUB")).test {
            assertSuccess()

            with(noteRepository.sendNoteEvents.awaitItem()) {
                assertThat(tags).containsExactly(
                    PubKeyTag(PUBKEY),
                )

                assertThat(content)
                    .isEqualTo("Thanks to #[0], for being #[0]")
            }
        }
    }

    @Test
    fun `post new note mentioning the same event multiple times`() = runTest {
        sendNote(SendNote.Params("Thanks to @$NOTE_BECH, for being @$NOTE_BECH")).test {
            assertSuccess()

            with(noteRepository.sendNoteEvents.awaitItem()) {
                assertThat(tags).containsExactly(
                    EventTag(NOTE_ID)
                )
                assertThat(content)
                    .isEqualTo("Thanks to #[0], for being #[0]")
            }

        }
    }


    @Test
    fun `reply to root note that doesn't contain mentions`() = runTest {
        sendNote(SendNote.Params(content = "test", parentNote = createNote())).test {
            assertSuccess()

            with(noteRepository.sendNoteEvents.awaitItem()) {
                assertThat(tags).containsExactly(
                    EventTag(NOTE_ID),
                    PubKeyTag(PUBKEY),
                ).inOrder()

                assertThat(content).isEqualTo("test")
            }
        }
    }

    @Test
    fun `reply to root note that contains mentions`() = runTest {
        val parentNote = createNote(
            tags = listOf(
                listOf("e", "002036bfff2a9779c840f718d2893ae8978416fdfb648ad929de59b13c78d61f40c6"),
                listOf("p", "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"),
            )
        )

        sendNote(
            SendNote.Params(
                content = "replying to another reply @$NPUB, @$NOTE_BECH",
                parentNote = parentNote
            )
        ).test {
            assertSuccess()

            with(noteRepository.sendNoteEvents.awaitItem()) {
                assertThat(tags).containsExactly(
                    EventTag(NoteId("002036bfff2a9779c840f718d2893ae8978416fdfb648ad929de59b13c78d61f40c6")),
                    EventTag(NOTE_ID),
                    PubKeyTag(PubKey.parse("npub1sg6plzptd64u62a878hep2kev88swjh3tw00gjsfl8f237lmu63q0uf63m")),
                    PubKeyTag(PUBKEY),
                ).inOrder()

                assertThat(content).isEqualTo("replying to another reply #[3], #[1]")
            }

        }
    }

    @Test
    fun `reply note that contains mentions`() = runTest {
        val parentNote = createNote(
            tags = listOf(
                listOf("e", "1f23d2882d58df3b080b4a2e2cf30165229dfed9bc35b2d53435453aa632d70b"),
                listOf("e", "00201f23d2882d58df3b080b4a2e2cf30165229dfed9bc35b2d53435453aa632d70b"),
                listOf("e", "00208e014a1399b4757e8b92b9eb3f343d416ed7631715545e96ca4fe3dd1e294ff4"),
                listOf("p", "762a3c15c6fa90911bf13d50fc3a29f1663dc1f04b4397a89eef604f622ecd60"),
                listOf("p", "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"),
            ),
            isReply = true,
        )

        sendNote(
            SendNote.Params(
                content = "replying to another reply @$NPUB, @$NOTE_BECH",
                parentNote = parentNote
            )
        ).test {
            assertSuccess()

            with(noteRepository.sendNoteEvents.awaitItem()) {
                assertThat(tags).containsExactly(
                    EventTag(NoteId("1f23d2882d58df3b080b4a2e2cf30165229dfed9bc35b2d53435453aa632d70b")),
                    EventTag(NOTE_ID),
                    PubKeyTag(PubKey.parse("npub1wc4rc9wxl2gfzxl384g0cw3f79nrms0sfdpe02y7aasy7c3we4sqd0qywr")),
                    PubKeyTag(PubKey.parse("npub1sg6plzptd64u62a878hep2kev88swjh3tw00gjsfl8f237lmu63q0uf63m")),
                    PubKeyTag(PUBKEY),
                ).inOrder()

                assertThat(content).isEqualTo("replying to another reply #[4], #[1]")
            }

        }

    }

    @Test
    fun `post new note with hashtags`() = runTest {
        sendNote(SendNote.Params("Thanks to @$NOTE_BECH, for being @$NOTE_BECH #foodstr #bitcoin")).test {
            assertSuccess()

            with(noteRepository.sendNoteEvents.awaitItem()) {
                assertThat(tags).containsExactly(
                    EventTag(NOTE_ID),
                    HashTag("foodstr"),
                    HashTag("bitcoin"),
                )
                assertThat(content)
                    .isEqualTo("Thanks to #[0], for being #[0] #foodstr #bitcoin")
            }

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
        assertThat(awaitItem()).isEqualTo(InvokeSuccess)
        awaitComplete()
    }

    companion object {
        private val NOTE_ID =
            NoteId("69dac2a7c46835a5c197eab632d262b84b8a017a1226739f08f163b6fede8c74")
        private val PUBKEY =
            PubKey("12bbde125d610b64f79194eb80478fe33ad95ed34184c7f0577e6214f3266cb0".decodeHex())

        private const val NPUB = "npub1z2aauyjavy9kfau3jn4cq3u0uvadjhkngxzv0uzh0e3pfuexdjcql0pyy7"

        private const val NOTE_BECH =
            "note1d8dv9f7ydq66tsvha2mr95nzhp9c5qt6zgn888cg793mdlk7336q0ha930"
    }
}

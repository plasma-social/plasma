package social.plasma.ui.mappers

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import fakes.FakeByteArrayPreference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import social.plasma.db.notes.NoteView
import social.plasma.db.notes.NoteWithUser
import social.plasma.db.reactions.FakeReactionDao
import social.plasma.db.usermetadata.UserMetadataEntity
import social.plasma.models.PubKey
import social.plasma.models.crypto.KeyGenerator
import social.plasma.nostr.models.Event
import social.plasma.nostr.relay.message.NostrMessageAdapter
import social.plasma.repository.FakeUserMetadataRepo
import social.plasma.ui.components.richtext.ProfileMention
import social.plasma.ui.notes.NoteContentParser
import social.plasma.ui.notes.NoteUiModel
import social.plasma.ui.util.FakeInstantFormatter
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class NoteCardMapperTest {
    private val instantFormatter = FakeInstantFormatter()
    private val mapper: NoteCardMapper
        get() {
            return NoteCardMapper(
                userMetaDataRepository = FakeUserMetadataRepo(),
                reactionDao = FakeReactionDao(),
                noteContentParser = NoteContentParser(),
                moshi = Moshi.Builder().add(NostrMessageAdapter())
                    .addLast(KotlinJsonAdapterFactory()).build(),
                myPubkeyPref = FakeByteArrayPreference(keyGenerator.generateKeyPair().pub.toByteArray()),
                instantFormatter = instantFormatter,
            )
        }

    @Test
    fun `Root kind 1 note`() = runTest {
        val noteWithUser = NoteWithUser(
            noteEntity = createNoteView(), userMetadataEntity = createUserMetadata()
        )

        with(mapper.toNoteUiModel(noteWithUser)) {
            assertThat(key).isEqualTo("test")
            assertThat(id).isEqualTo("test")
            assertThat(name).isEqualTo("@test")
            assertThat(displayName).isEqualTo("Test")
            assertThat(avatarUrl).isEqualTo("testpicture")
            assertThat(nip5Identifier).isNull()
            assertThat(cardLabel).isNull()
            assertThat(replyCount).isEmpty()
            assertThat(timePosted).isEqualTo(instantFormatter.formatResponse.value)
            assertThat(shareCount).isEmpty()
            assertThat(likeCount).isEqualTo(40)
            assertThat(userPubkey).isEqualTo(pubkey)
            assertThat(nip5Domain).isNull()
        }
    }

    @Test
    fun `Reply kind 1 note`() = runTest {

        val noteWithUser = NoteWithUser(
            noteEntity = createNoteView(
                tags = listOf(
                    listOf("e", rootNoteIdHex), listOf("p", pubkeyHex)
                ),
                isReply = true,
            ), userMetadataEntity = createUserMetadata()
        )

        with(mapper.toNoteUiModel(noteWithUser)) {
            assertThat(key).isEqualTo("test")
            assertThat(id).isEqualTo("test")
            assertThat(name).isEqualTo("@test")
            assertThat(displayName).isEqualTo("Test")
            assertThat(avatarUrl).isEqualTo("testpicture")
            assertThat(nip5Identifier).isNull()
            assertThat(cardLabel).isEqualTo("Replying to ${PubKey(pubkeyHex).shortBech32}")
            assertThat(replyCount).isEmpty()
            assertThat(timePosted).isEqualTo(instantFormatter.formatResponse.value)
            assertThat(shareCount).isEmpty()
            assertThat(likeCount).isEqualTo(40)
            assertThat(userPubkey).isEqualTo(pubkey)
            assertThat(nip5Domain).isNull()
        }
    }

    @Test
    fun `Reply kind 6 note`() = runTest {
        val noteWithUser = NoteWithUser(
            noteEntity = createNoteView(
                kind = Event.Kind.Repost,
                content = "{\"pubkey\":\"978c8f26ea9b3c58bfd4c8ddfde83741a6c2496fab72774109fe46819ca49708\",\"content\":\"\uD83E\uDD19\uD83C\uDFFB\",\"id\":\"d99979490a9b6b0d5847425b5812fbc82f409d8bdca4f2b61d9c95106bfb1141\",\"created_at\":1678754891,\"sig\":\"520a144cd5cec9d6850ce101c6ceaaed4f4bf2f28e7891bd7a2c83e527eed394f0928ccacb15fff30e1647ccf50d903765400b1b6c62d7abe1af97014339c5f9\",\"kind\":1,\"tags\":[[\"e\",\"ec6c4987b2177402b71bf1ea958ae2dac3e45f45fdcd9e2ef5e3239925cbcbbe\"],[\"p\",\"a9f8b3f2ac19cc06d5194dd1ac9314d4741a09777444986553926d9165181647\"]]}"
            ), userMetadataEntity = createUserMetadata()
        )

        with(mapper.toNoteUiModel(noteWithUser)) {
            assertThat(headerContent).isEqualTo(
                NoteUiModel.ContentBlock.Text(
                    "Boosted by #[0]",
                    mapOf(0 to ProfileMention("@test", pubkey))
                )
            )
            assertThat(key).isEqualTo("test")
            assertThat(id).isEqualTo("d99979490a9b6b0d5847425b5812fbc82f409d8bdca4f2b61d9c95106bfb1141")
            assertThat(name).isEqualTo("j7xg7fh2:yqj8lafh")
            assertThat(displayName).isEqualTo("j7xg7fh2:yqj8lafh")
            assertThat(avatarUrl).isNull()
            assertThat(nip5Identifier).isNull()
            assertThat(replyCount).isEmpty()
            assertThat(timePosted).isEqualTo(instantFormatter.formatResponse.value)
            assertThat(shareCount).isEmpty()
            assertThat(likeCount).isEqualTo(0)
            assertThat(userPubkey).isEqualTo(PubKey("978c8f26ea9b3c58bfd4c8ddfde83741a6c2496fab72774109fe46819ca49708"))
            assertThat(nip5Domain).isNull()
            assertThat(cardLabel).isEqualTo("Replying to 48ut8u4v:rs59ty0w")
        }
    }

    @Test
    fun `Kind 6 with unknown boost author`() = runTest {
        val noteWithUser = NoteWithUser(
            noteEntity = createNoteView(
                kind = Event.Kind.Repost,
                content = "{\"pubkey\":\"978c8f26ea9b3c58bfd4c8ddfde83741a6c2496fab72774109fe46819ca49708\",\"content\":\"\uD83E\uDD19\uD83C\uDFFB\",\"id\":\"d99979490a9b6b0d5847425b5812fbc82f409d8bdca4f2b61d9c95106bfb1141\",\"created_at\":1678754891,\"sig\":\"520a144cd5cec9d6850ce101c6ceaaed4f4bf2f28e7891bd7a2c83e527eed394f0928ccacb15fff30e1647ccf50d903765400b1b6c62d7abe1af97014339c5f9\",\"kind\":1,\"tags\":[[\"e\",\"ec6c4987b2177402b71bf1ea958ae2dac3e45f45fdcd9e2ef5e3239925cbcbbe\"],[\"p\",\"a9f8b3f2ac19cc06d5194dd1ac9314d4741a09777444986553926d9165181647\"]]}"
            ),
            userMetadataEntity = null,
        )

        with(mapper.toNoteUiModel(noteWithUser)) {
            assertThat(headerContent).isEqualTo(
                NoteUiModel.ContentBlock.Text(
                    "Boosted by #[0]",
                    mapOf(0 to ProfileMention(pubkey.shortBech32, pubkey))
                )
            )
        }
    }

    @Test
    fun `Kind 6 with invalid content`() = runTest {
        val noteWithUser = NoteWithUser(
            noteEntity = createNoteView(
                kind = Event.Kind.Repost,
                content = "{}"
            ), userMetadataEntity = createUserMetadata()
        )

        with(mapper.toNoteUiModel(noteWithUser)) {
            assertThat(hidden).isTrue()
        }
    }

    private fun createUserMetadata(pubkey: String = pubkeyHex) = UserMetadataEntity(
        pubkey = pubkey,
        name = "@test",
        about = null,
        picture = "testpicture",
        banner = null,
        lud = null,
        displayName = "Test",
        createdAt = 0,
        nip05 = null,
        website = null,
    )

    private fun createNoteView(
        pubkey: String = pubkeyHex,
        tags: List<List<String>> = emptyList(),
        isReply: Boolean = false,
        kind: Int = Event.Kind.Note,
        content: String = "Test",
    ) = NoteView(
        id = "test",
        content = content,
        createdAt = Instant.now().epochSecond,
        kind = kind,
        isReply = isReply,
        reactionCount = 40,
        pubkey = pubkey,
        tags = tags,
    )

    companion object {
        private val keyGenerator = KeyGenerator()
        val pubkey = PubKey.of(keyGenerator.generateKeyPair().pub.toByteArray())
        const val rootNoteIdHex = "fe64ae6fdd89581938b4644958ffcefba5871bd9ad9f4b5d3dbe59e7274faaf6"
        val pubkeyHex = pubkey.hex
    }
}

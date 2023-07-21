package social.plasma.feeds.presenters

import app.cash.nostrino.crypto.PubKey
import app.cash.nostrino.crypto.SecKeyGenerator
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeHex
import org.junit.Test
import shortBech32
import social.plasma.domain.interactors.GetNip5Status
import social.plasma.features.feeds.presenters.R
import social.plasma.features.feeds.screens.feed.ContentBlock
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.feeds.presenters.feed.NoteCardMapper
import social.plasma.feeds.presenters.feed.NoteContentParser
import social.plasma.models.Event
import social.plasma.models.NoteId
import social.plasma.models.NoteView
import social.plasma.models.NoteWithUser
import social.plasma.models.ProfileMention
import social.plasma.models.UserMetadataEntity
import social.plasma.models.crypto.Bech32
import social.plasma.nostr.relay.message.NostrMessageAdapter
import social.plasma.shared.repositories.fakes.FakeAccountStateRepository
import social.plasma.shared.repositories.fakes.FakeNip5Validator
import social.plasma.shared.repositories.fakes.FakeNoteRepository
import social.plasma.shared.repositories.fakes.FakeUserMetadataRepository
import social.plasma.shared.utils.fakes.FakeInstantFormatter
import java.time.Instant

class NoteCardMapperTest {
    private val instantFormatter = FakeInstantFormatter()
    private val stringManager = social.plasma.shared.utils.fakes.FakeStringManager(
        R.string.replying_to_single to "Replying to {user}",
        R.string.replying_to_many to "{additionalUserCount, plural,\n" +
                "            =0 {Replying to {firstUser} and {secondUser}}\n" +
                "            =1 {Replying to {firstUser}, {secondUser}, and 1 other}\n" +
                "            other {Replying to {firstUser}, {secondUser}, and {additionalUserCount} others}" +
                "        }",
    )

    private val moshi = Moshi.Builder()
        .add(NostrMessageAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val TestScope.mapper: NoteCardMapper
        get() {
            return NoteCardMapper(
                userMetaDataRepository = FakeUserMetadataRepository(),
                accountStateRepository = FakeAccountStateRepository(publicKey = SecKeyGenerator().generate().pubKey.key.toByteArray()),
                noteRepository = FakeNoteRepository(),
                noteContentParser = NoteContentParser(),
                moshi = moshi,
                instantFormatter = instantFormatter,
                stringManager = stringManager,
                getNip5Status = GetNip5Status(FakeNip5Validator(), coroutineContext)
            )
        }

    @Test
    fun `Root kind 1 note`() = runTest {
        val noteWithUser = NoteWithUser(
            noteEntity = createNoteView(), userMetadataEntity = createUserMetadata()
        )

        with(mapper.toFeedItem(noteWithUser) as FeedItem.NoteCard) {
            assertThat(key).isEqualTo("test")
            assertThat(id).isEqualTo("test")
            assertThat(name).isEqualTo("@test")
            assertThat(displayName).isEqualTo("Test")
            assertThat(avatarUrl).isEqualTo("testpicture")
            assertThat(nip5Identifier).isNull()
            assertThat(cardLabel).isNull()
            assertThat(replyCount).isEmpty()
            assertThat(timePosted).isEqualTo(instantFormatter.formatResponse)
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

        with(mapper.toFeedItem(noteWithUser) as FeedItem.NoteCard) {
            assertThat(key).isEqualTo("test")
            assertThat(id).isEqualTo("test")
            assertThat(name).isEqualTo("@test")
            assertThat(displayName).isEqualTo("Test")
            assertThat(avatarUrl).isEqualTo("testpicture")
            assertThat(nip5Identifier).isNull()
            assertThat(cardLabel).isEqualTo("Replying to ${pubkey.shortBech32()}")
            assertThat(replyCount).isEmpty()
            assertThat(timePosted).isEqualTo(instantFormatter.formatResponse)
            assertThat(shareCount).isEmpty()
            assertThat(likeCount).isEqualTo(40)
            assertThat(userPubkey).isEqualTo(pubkey)
            assertThat(nip5Domain).isNull()
        }
    }

    @Test
    fun `Reply with a single p tag uses correct string`() = runTest {
        val referencedPubkey = secKeyGenerator.generate().pubKey

        val noteWithUser = NoteWithUser(
            noteEntity = createNoteView(
                tags = listOf(
                    listOf("p", referencedPubkey.key.hex())
                ),
                isReply = true,
            ), userMetadataEntity = createUserMetadata()
        )

        with(mapper.toFeedItem(noteWithUser) as FeedItem.NoteCard) {
            assertThat(cardLabel).isEqualTo("Replying to ${referencedPubkey.shortBech32()}")
        }
    }

    @Test
    fun `Reply note with many tags only shows the first two`() = runTest {
        val referencedPubkeys = (1..50).map { secKeyGenerator.generate().pubKey }

        val noteWithUser = NoteWithUser(
            noteEntity = createNoteView(
                tags = referencedPubkeys.map {
                    listOf("p", it.key.hex())
                },
                isReply = true,
            ), userMetadataEntity = createUserMetadata()
        )

        with(mapper.toFeedItem(noteWithUser) as FeedItem.NoteCard) {
            assertThat(cardLabel).isEqualTo("Replying to ${referencedPubkeys[0].shortBech32()}, ${referencedPubkeys[1].shortBech32()}, and 48 others")
        }
    }

    @Test
    fun `Reply note with two tags shows correct text`() = runTest {
        val referencedPubkeys = (1..2).map { secKeyGenerator.generate().pubKey }

        val noteWithUser = NoteWithUser(
            noteEntity = createNoteView(
                tags = referencedPubkeys.map {
                    listOf("p", it.key.hex())
                },
                isReply = true,
            ), userMetadataEntity = createUserMetadata()
        )

        with(mapper.toFeedItem(noteWithUser) as FeedItem.NoteCard) {
            assertThat(cardLabel).isEqualTo("Replying to ${referencedPubkeys[0].shortBech32()} and ${referencedPubkeys[1].shortBech32()}")
        }
    }

    @Test
    fun `Reply note with 1 "other" tag shows singular text`() = runTest {
        val referencedPubkeys = (1..3).map { secKeyGenerator.generate().pubKey }

        val noteWithUser = NoteWithUser(
            noteEntity = createNoteView(
                tags = referencedPubkeys.map {
                    listOf("p", it.key.hex())
                },
                isReply = true,
            ), userMetadataEntity = createUserMetadata()
        )

        with(mapper.toFeedItem(noteWithUser) as FeedItem.NoteCard) {
            assertThat(cardLabel).isEqualTo("Replying to ${referencedPubkeys[0].shortBech32()}, ${referencedPubkeys[1].shortBech32()}, and 1 other")
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

        with(mapper.toFeedItem(noteWithUser) as FeedItem.NoteCard) {
            assertThat(headerContent).isEqualTo(
                ContentBlock.Text(
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
            assertThat(timePosted).isEqualTo(instantFormatter.formatResponse)
            assertThat(shareCount).isEmpty()
            assertThat(likeCount).isEqualTo(0)
            assertThat(userPubkey).isEqualTo(PubKey("978c8f26ea9b3c58bfd4c8ddfde83741a6c2496fab72774109fe46819ca49708".decodeHex()))
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

        with(mapper.toFeedItem(noteWithUser) as FeedItem.NoteCard) {
            assertThat(headerContent).isEqualTo(
                ContentBlock.Text(
                    "Boosted by #[0]",
                    mapOf(0 to ProfileMention(pubkey.shortBech32(), pubkey))
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

        with(mapper.toFeedItem(noteWithUser) as FeedItem.NoteCard) {
            assertThat(hidden).isTrue()
        }
    }

    @Test
    fun `Kind 1 with quoted note`() = runTest {
        val noteWithUser = NoteWithUser(
            noteEntity = createNoteView(
                content = "Amazing:nostr:note149mprnkqdyjqvzave4m3y9lmea42pq4s6dz04rwtnkk35qs28m9qml92ex",
            ),
            userMetadataEntity = createUserMetadata(),
        )

        with(mapper.toFeedItem(noteWithUser) as FeedItem.NoteCard) {
            assertThat(content).containsExactly(
                ContentBlock.Text(
                    "Amazing:",
                    emptyMap()
                ),
                ContentBlock.NoteQuote(
                    NoteId.of(Bech32.decodeBytes("note149mprnkqdyjqvzave4m3y9lmea42pq4s6dz04rwtnkk35qs28m9qml92ex").second),
                )
            )
        }
    }

    private fun createUserMetadata(pubkey: String = pubkeyHex) = UserMetadataEntity(
        pubkey = pubkey,
        name = "@test",
        about = null,
        picture = "testpicture",
        banner = null,
        lud06 = null,
        lud16 = null,
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
        private val secKeyGenerator = SecKeyGenerator()
        private val pubkey = secKeyGenerator.generate().pubKey
        const val rootNoteIdHex = "fe64ae6fdd89581938b4644958ffcefba5871bd9ad9f4b5d3dbe59e7274faaf6"
        private val pubkeyHex = pubkey.key.hex()
    }
}

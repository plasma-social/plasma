package social.plasma.nostr.message

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.orNull
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import okio.ByteString
import social.plasma.nostr.BuildingBlocks.moshi
import social.plasma.models.Event
import social.plasma.nostr.models.EventSerdeTest.Companion.arbEvent
import social.plasma.nostr.models.EventSerdeTest.Companion.arbVanillaString
import social.plasma.models.TypedEvent
import social.plasma.nostr.models.UserMetaData
import social.plasma.nostr.relay.message.RealEventRefiner
import social.plasma.nostr.relay.message.RelayMessage
import java.time.Instant
import java.util.UUID

class EventRefinerTest : StringSpec({

    "converts a flow of type 0 into user meta data" {
        checkAll(arbUserMetadataTestData) { (event, userMetaData) ->
            val flow = flow { emit(event) }
            val refined = flow.map { RealEventRefiner(moshi).toUserMetaData(it) }.take(1).toList()
            refined shouldContainExactly listOf(
                with(event.event) {
                    TypedEvent(id, pubKey, createdAt, Event.Kind.MetaData, tags, userMetaData, sig)
                }
            )
        }
    }

    "converts type 3 notes to relay information list" {
        val message =
            createRelayMessage(content = "{\"wss://nostr-pub.semisol.dev\": { \"read\":true, \"write\":true}, \"wss://relay.damus.io\": { \"read\":true, \"write\":true}}")

        val typed = RealEventRefiner(moshi).toRelayDetailList(message)

        typed shouldNotBe null
        typed!!.content.count() shouldBe 2
    }

    "converts type 3 notes to null relay information list" {
        val message =
            createRelayMessage(content = "")

        val typed = RealEventRefiner(moshi).toRelayDetailList(message)

        typed shouldBe null
    }
}) {

    companion object {
        private val userMetaDataAdapter = moshi.adapter(UserMetaData::class.java)

        private val arbUserMetaData: Arb<UserMetaData> =
            Arb.bind(
                arbVanillaString.orNull(0.1),
                arbVanillaString.orNull(0.1),
                arbVanillaString.orNull(0.1),
                arbVanillaString.orNull(0.1),
                arbVanillaString.orNull(0.1),
                arbVanillaString.orNull(0.1),
                arbVanillaString.orNull(0.1),
                arbVanillaString.orNull(0.1),
            ) { name, about, picture, banner, nip5, website, displayName, lud ->
                UserMetaData(name, about, picture, banner, nip5, website, displayName, lud)
            }

        private val arbUserMetadataTestData: Arb<Pair<RelayMessage.EventRelayMessage, UserMetaData>> =
            Arb.bind(arbVanillaString, arbEvent, arbUserMetaData) { subId, event, userMetaData ->
                val correctEvent = event.copy(
                    kind = Event.Kind.MetaData,
                    content = userMetaDataAdapter.toJson(userMetaData)
                )
                RelayMessage.EventRelayMessage(subId, correctEvent) to userMetaData
            }
    }
}

private fun createRelayMessage(
    content: String,
) = RelayMessage.EventRelayMessage(
    subscriptionId = UUID.randomUUID().toString(),
    event = Event(
        id = ByteString.EMPTY,
        pubKey = ByteString.EMPTY,
        sig = ByteString.EMPTY,
        kind = Event.Kind.ContactList,
        createdAt = Instant.now(),
        tags = emptyList(),
        content = content
    )
)

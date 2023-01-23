package social.plasma.nostr.message

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.orNull
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import social.plasma.nostr.BuildingBlocks.moshi
import social.plasma.nostr.models.Event
import social.plasma.nostr.models.EventSerdeTest.Companion.arbEvent
import social.plasma.nostr.models.EventSerdeTest.Companion.arbVanillaString
import social.plasma.nostr.models.TypedEvent
import social.plasma.nostr.models.UserMetaData
import social.plasma.nostr.relay.message.EventRefiner
import social.plasma.nostr.relay.message.RelayMessage

class EventRefinerTest : StringSpec({

    "converts a flow of type 0 into user meta data" {
        checkAll(arbTestData) { (event, userMetaData) ->
            val flow = flow { emit(event) }
            val refined = flow.map { EventRefiner(moshi).toUserMetaData(it) }.take(1).toList()
            refined shouldContainExactly listOf(
                with(event.event) {
                    TypedEvent(id, pubKey, createdAt, Event.Kind.MetaData, tags, userMetaData, sig)
                }
            )
        }
    }
}) {

    companion object {
        private val userMetaDataAdapter = moshi.adapter(UserMetaData::class.java)

        val arbUserMetaData: Arb<UserMetaData> =
            Arb.bind(
                arbVanillaString.orNull(0.1),
                arbVanillaString.orNull(0.1),
                arbVanillaString.orNull(0.1),
                arbVanillaString.orNull(0.1)
            ) { name, about, picture, displayName ->
                UserMetaData(name, about, picture, displayName)
            }

        private val arbTestData: Arb<Pair<RelayMessage.EventRelayMessage, UserMetaData>> =
            Arb.bind(arbVanillaString, arbEvent, arbUserMetaData) { subId, event, userMetaData ->
                val correctEvent = event.copy(
                    kind = Event.Kind.MetaData,
                    content = userMetaDataAdapter.toJson(userMetaData)
                )
                RelayMessage.EventRelayMessage(subId, correctEvent) to userMetaData
            }
    }
}
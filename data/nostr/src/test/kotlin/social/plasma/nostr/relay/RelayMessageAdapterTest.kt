package social.plasma.nostr.relay

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import social.plasma.nostr.models.EventCountSerdeTest.Companion.arbCountEvent
import social.plasma.nostr.models.EventSerdeTest.Companion.arbEvent
import social.plasma.nostr.models.EventSerdeTest.Companion.arbVanillaString
import social.plasma.nostr.relay.message.NostrMessageAdapter
import social.plasma.nostr.relay.message.RelayMessage
import social.plasma.nostr.relay.message.RelayMessage.EventRelayMessage
import social.plasma.nostr.relay.message.RelayMessage.NoticeRelayMessage
import java.util.UUID

class RelayMessageAdapterTest : StringSpec({

    val subject = Moshi.Builder()
        .add(NostrMessageAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build().adapter(RelayMessage::class.java)

    "can serde relay messages" {
        checkAll(arbRelayMessage) { message ->
            val json = subject.toJson(message)
            subject.fromJson(json) shouldBe message
        }
    }

}) {
    companion object {

        private val arbNoticeRelayMessage: Arb<NoticeRelayMessage> =
            arbVanillaString.map { NoticeRelayMessage(it) }

        private val arbEventRelayMessage: Arb<EventRelayMessage> = Arb.bind(
            arbitrary { UUID.randomUUID().toString() },
            arbEvent
        ) { subscriptionId, event ->
            EventRelayMessage(subscriptionId, event)
        }

        private val arbCountRelayMessage = Arb.bind(
            arbitrary { UUID.randomUUID().toString() },
            arbCountEvent
        ) { subscriptionId, event ->
            RelayMessage.CountRelayMessage(subscriptionId, event)
        }

        val arbRelayMessage =
            Arb.choice(arbNoticeRelayMessage, arbEventRelayMessage, arbCountRelayMessage)
    }
}

package social.plasma.relay.message

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import social.plasma.models.EventSerdeTest.Companion.arbEvent
import social.plasma.relay.message.RelayMessage.EventRelayMessage
import social.plasma.relay.message.RelayMessage.NoticeRelayMessage
import java.util.*

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

        val arbNoticeRelayMessage: Arb<NoticeRelayMessage> =
            Arb.stringPattern("[A-Za-z0-9 ]+")
                .map { NoticeRelayMessage(it) }

        val arbEventRelayMessage: Arb<EventRelayMessage> = Arb.bind(
            arbitrary { UUID.randomUUID().toString() },
            arbEvent
        ) { subscriptionId, event ->
            EventRelayMessage(subscriptionId, event)
        }

        val arbRelayMessage = Arb.choice(arbNoticeRelayMessage, arbEventRelayMessage)
    }
}
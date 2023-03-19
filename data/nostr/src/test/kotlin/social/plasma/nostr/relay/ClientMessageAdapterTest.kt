package social.plasma.nostr.relay

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.checkAll
import social.plasma.nostr.message.CloseMessageAdapterTest.Companion.arbCloseMessage
import social.plasma.nostr.message.RequestMessageAdapterTest.Companion.arbSubscribeMessage
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.NostrMessageAdapter

class ClientMessageAdapterTest: StringSpec({

    val subject = Moshi.Builder()
        .add(NostrMessageAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build().adapter(ClientMessage::class.java)

    "can serde client messages" {
        checkAll(arbClientMessage) { message ->
            val json = subject.toJson(message)
            subject.fromJson(json) shouldBe message
        }
    }

}) {
    companion object {
        private val arbClientMessage = Arb.choice(arbCloseMessage, arbSubscribeMessage)
    }
}
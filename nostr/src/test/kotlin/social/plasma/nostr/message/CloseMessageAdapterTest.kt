package social.plasma.nostr.message

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import social.plasma.nostr.models.EventSerdeTest.Companion.arbVanillaString
import social.plasma.nostr.relay.message.ClientMessage.UnsubscribeMessage
import social.plasma.nostr.relay.message.NostrMessageAdapter

class CloseMessageAdapterTest : StringSpec({

    val moshi = Moshi.Builder()
        .add(NostrMessageAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()
    val adapter = moshi.adapter(UnsubscribeMessage::class.java)

    "can serde requests" {
        checkAll(arbCloseMessage) { request ->
            val json = adapter.toJson(request)
            adapter.fromJson(json) shouldBe request
        }
    }
}) {
    companion object {
        val arbCloseMessage = arbVanillaString.map { UnsubscribeMessage(it) }
    }
}

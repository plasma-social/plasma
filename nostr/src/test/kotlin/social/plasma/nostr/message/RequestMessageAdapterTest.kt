package social.plasma.nostr.message

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.checkAll
import social.plasma.nostr.models.EventSerdeTest.Companion.arbByteString32
import social.plasma.nostr.models.EventSerdeTest.Companion.arbInstantSeconds
import social.plasma.nostr.models.EventSerdeTest.Companion.arbVanillaString
import social.plasma.nostr.relay.message.Filters
import social.plasma.nostr.relay.message.NostrMessageAdapter
import social.plasma.nostr.relay.message.SubscribeMessage

class RequestMessageAdapterTest : StringSpec({

    val moshi = Moshi.Builder()
        .add(NostrMessageAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()
    val adapter = moshi.adapter(SubscribeMessage::class.java)

    "can serde requests" {
        checkAll(arbRequestMessage) { request ->
            val json = adapter.toJson(request)
            adapter.fromJson(json) shouldBe request
        }
    }
}) {
    companion object {
        private val arbFilters: Arb<Filters> = Arb.bind(
            arbInstantSeconds,
            Arb.set(arbByteString32.map { it.hex() }, 0..10),
            Arb.set(Arb.int(0..99), 0..10)
        ) { since, authors, kinds -> Filters(since, authors, kinds) }

        val arbRequestMessage = Arb.bind(
            arbVanillaString,
            arbFilters
        ) { subId, filter -> SubscribeMessage(subId, filter) }
    }
}

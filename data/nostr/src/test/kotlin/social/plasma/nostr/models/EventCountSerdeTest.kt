package social.plasma.nostr.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import social.plasma.models.EventCount
import social.plasma.nostr.relay.message.NostrMessageAdapter

class EventCountSerdeTest : StringSpec({

    val subject = Moshi.Builder()
        .add(NostrMessageAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build().adapter(EventCount::class.java)

    "can serde count events" {
        checkAll(arbCountEvent) { event ->
            val json = subject.toJson(event)
            subject.fromJson(json) shouldBe event
        }
    }
}) {
    companion object {
        val arbCountEvent: Arb<EventCount> = Arb.long(0, 100).map { EventCount(it) }
    }
}

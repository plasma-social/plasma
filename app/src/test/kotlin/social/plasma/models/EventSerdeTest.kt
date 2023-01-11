package social.plasma.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import okio.ByteString
import okio.ByteString.Companion.toByteString
import social.plasma.relay.message.NostrMessageAdapter
import java.time.Instant
import java.time.temporal.ChronoUnit

class EventSerdeTest : StringSpec({

    val subject = Moshi.Builder()
        .add(NostrMessageAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build().adapter(Event::class.java)

    "can serde events" {
        checkAll(arbEvent) { event ->
            val json = subject.toJson(event)
            subject.fromJson(json) shouldBe event
        }
    }

}) {
    companion object {
        val arbVanillaString: Arb<String> = Arb.stringPattern("[A-Za-z0-9 ]{1,24}")

        val arbByteString32: Arb<ByteString> = Arb.list(Arb.byte(), 32..32)
            .map { it.toByteArray().toByteString() }

        private val arbByteString64: Arb<ByteString> = Arb.list(Arb.byte(), 64..64)
            .map { it.toByteArray().toByteString() }

        val arbInstantSeconds: Arb<Instant> =
            Arb.instant(Instant.EPOCH, Instant.now().plus(5000, ChronoUnit.DAYS))
                .map { it.truncatedTo(ChronoUnit.SECONDS) }

        val arbEvent: Arb<Event> = Arb.bind(
            arbByteString32,
            arbByteString32,
            arbInstantSeconds,
            Arb.list(Arb.list(arbVanillaString, 1..5), 0..10),
            arbVanillaString,
            arbByteString64
        ) { id, pubKey, createdAt, tags, content, sig ->
            Event(id, pubKey, createdAt, 1, tags, content, sig)
        }
    }
}
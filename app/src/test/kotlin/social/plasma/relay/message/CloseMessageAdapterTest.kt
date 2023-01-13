package social.plasma.relay.message

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import social.plasma.models.EventSerdeTest.Companion.arbByteString32
import social.plasma.models.EventSerdeTest.Companion.arbInstantSeconds
import social.plasma.models.EventSerdeTest.Companion.arbVanillaString

class CloseMessageAdapterTest : StringSpec({

    val moshi = Moshi.Builder()
        .add(NostrMessageAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()
    val adapter = moshi.adapter(CloseMessage::class.java)

    "can serde requests" {
        checkAll(arbCloseMessage) { request ->
            val json = adapter.toJson(request)
            adapter.fromJson(json) shouldBe request
        }
    }
}) {
    companion object {
        val arbCloseMessage = arbVanillaString.map { CloseMessage(it) }
    }
}
package social.plasma.relay.message

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll
import social.plasma.models.EventSerdeTest.Companion.arbInstantSeconds

class RequestMessageAdapterTest : StringSpec({

    val moshi = Moshi.Builder()
        .add(NostrMessageAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()
    val adapter = moshi.adapter(RequestMessage::class.java)

    "can serde requests" {
        checkAll(arbRequestMessage) { request ->
            val json = adapter.toJson(request)
            adapter.fromJson(json) shouldBe request
        }
    }
}) {
    companion object {
        private val arbFilters: Arb<Filters> = arbInstantSeconds.map { Filters(since = it) }

        val arbRequestMessage = Arb.bind(
            Arb.stringPattern("[A-Za-z0-9 ]+"),
            arbFilters
        ) { subId, filter -> RequestMessage(subId, filter) }
    }
}
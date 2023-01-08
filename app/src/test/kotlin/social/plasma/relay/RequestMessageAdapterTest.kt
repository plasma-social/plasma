package social.plasma.relay

import com.squareup.moshi.Moshi
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.checkAll
import social.plasma.relay.json.RequestMessageAdapter

class RequestMessageAdapterTest : StringSpec({

    val moshi = Moshi.Builder().add(RequestMessageAdapter()).build()
    val adapter = moshi.adapter(RequestMessage::class.java)

    "can serde requests" {
        checkAll(arbRequest) { request ->
            val json = adapter.toJson(request)
            json shouldBe """["REQ","${request.subscriptionId}","{}"]"""
            adapter.fromJson(json) shouldBe request
        }
    }
}) {
    companion object {
        val arbRequest = arbitrary { RequestMessage() }
    }
}
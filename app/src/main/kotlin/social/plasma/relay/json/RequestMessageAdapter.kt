package social.plasma.relay.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import social.plasma.relay.RequestMessage

class RequestMessageAdapter {
    @ToJson
    fun toJson(request: RequestMessage) = listOf("REQ", request.subscriptionId, "{}")

    @FromJson
    fun fromJson(array: List<String>): RequestMessage {
        val (_, sub) = array
        return RequestMessage(sub)
    }
}
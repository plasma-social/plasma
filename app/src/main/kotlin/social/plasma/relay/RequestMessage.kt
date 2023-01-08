package social.plasma.relay

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.*

data class RequestMessage(
    val subscriptionId: String = "plasma-request-${UUID.randomUUID()}",
)


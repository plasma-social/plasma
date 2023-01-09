package social.plasma.relay.message

import java.time.Instant
import java.util.*

data class RequestMessage(
    val subscriptionId: String = "plasma-request-${UUID.randomUUID()}",
    val filters: Filters = Filters(since = Instant.now())
)


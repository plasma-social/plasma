package social.plasma.relay.message

import java.util.*

data class SubscribeMessage(
    val subscriptionId: String = "plasma-request-${UUID.randomUUID()}",
    val filters: Filters
)
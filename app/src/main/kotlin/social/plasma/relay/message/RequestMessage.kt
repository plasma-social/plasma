package social.plasma.relay.message

import java.util.*

data class RequestMessage(
    val subscriptionId: String = "plasma-request-${UUID.randomUUID()}",
    val filters: Filters
) {
    fun toCloseMessage() = CloseMessage(subscriptionId)
}


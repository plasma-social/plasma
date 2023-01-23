package social.plasma.nostr.relay.message

import java.util.UUID

data class SubscribeMessage(
    val subscriptionId: String = "plasma-request-${UUID.randomUUID()}",
    val filters: Filters,
)

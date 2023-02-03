package social.plasma.nostr.relay.message

import java.util.*

/** A message sent from a client to a relay */
sealed class ClientMessage {

    data class SubscribeMessage(
        val subscriptionId: String,
        val filter: Filter,
        val additionalFilters: List<Filter>,
    ): ClientMessage() {

        constructor(filter: Filter, vararg additionalFilters: Filter) : this(
            subscriptionId = "plasma-request-${UUID.randomUUID()}",
            filter = filter,
            additionalFilters = additionalFilters.toList()
        )

        val filters = listOf(filter).plus(additionalFilters)
    }


    data class UnsubscribeMessage(val subscriptionId: String): ClientMessage()

}

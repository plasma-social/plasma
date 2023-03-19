package social.plasma.nostr.relay.message

import social.plasma.models.Event
import java.util.UUID

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

    data class EventMessage(
        val event: Event,
    ) : ClientMessage()

}

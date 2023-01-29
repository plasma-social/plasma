package social.plasma.nostr.relay.message

import java.util.*

data class SubscribeMessage(
    val subscriptionId: String,
    val filter: Filter,
    val additionalFilters: List<Filter>,
) {

    constructor(filter: Filter, vararg additionalFilters: Filter) : this(
        subscriptionId = "plasma-request-${UUID.randomUUID()}",
        filter = filter,
        additionalFilters = additionalFilters.toList()
    )

    val filters = listOf(filter).plus(additionalFilters)

}

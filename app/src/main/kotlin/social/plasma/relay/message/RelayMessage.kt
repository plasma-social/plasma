package social.plasma.relay.message

import social.plasma.models.Event

sealed class RelayMessage {

    data class EventRelayMessage(
        val subscriptionId: String,
        val event: Event,
    ) : RelayMessage()

    data class NoticeRelayMessage(
        val message: String,
    ) : RelayMessage()

}


package social.plasma.relay

sealed class RelayMessage {

    data class EventRelayMessage(
        val subscriptionId: String,
        val event: Event,
    ) : RelayMessage()

    data class NoticeRelayMessage(
        val message: String,
    ) : RelayMessage()

}


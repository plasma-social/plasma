package social.plasma.relay

class Subscription(
    val id: String,
    val close: () -> Unit,
)
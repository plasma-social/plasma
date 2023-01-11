package social.plasma.relay.message

import java.time.Instant

data class Filters(
    val since: Instant = Instant.now(),
    val authors: Set<String> = emptySet(),
    val kinds: Set<Int> = emptySet(),
)

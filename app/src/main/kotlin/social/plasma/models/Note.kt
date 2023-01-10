package social.plasma.models

import java.time.Instant

/** Notes are derived from events of kind 1 */
data class Note(
    val content: String,
    val pubKey: String,
    val createdAt: Instant,
)

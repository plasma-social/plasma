package social.plasma.models

import java.time.Instant

data class Note(
    val content: String,
    val pubKey: String,
    val createdAt: Instant,
)

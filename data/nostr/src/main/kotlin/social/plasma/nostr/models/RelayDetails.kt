package social.plasma.nostr.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RelayDetails(
    val read: Boolean,
    val write: Boolean,
)

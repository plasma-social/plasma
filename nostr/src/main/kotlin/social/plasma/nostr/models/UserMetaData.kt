package social.plasma.nostr.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserMetaData(
    val name: String?,
    val about: String?,
    val picture: String?,
    val displayName: String?,
)

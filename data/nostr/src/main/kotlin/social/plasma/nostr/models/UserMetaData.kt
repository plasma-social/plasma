package social.plasma.nostr.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserMetaData(
    val name: String?,
    val about: String?,
    val picture: String?,
    val banner: String?,
    val nip05: String?,
    val website: String?,
    @Json(name = "display_name")
    val displayName: String?,
    @Json(name = "lud06")
    val lud: String?,
)

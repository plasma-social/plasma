package social.plasma.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Reaction(
    val text: String,
)

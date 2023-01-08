package social.plasma.relay

import com.squareup.moshi.Json
import okio.ByteString
import java.time.Instant

data class Event(
    val id: ByteString,
    @Json(name = "pubkey")
    val pubKey: ByteString,
    @Json(name = "created_at")
    val createdAt: Instant,
    val kind: Int,
    // TODO tags
    val content: String,
    val sig: ByteString,
)

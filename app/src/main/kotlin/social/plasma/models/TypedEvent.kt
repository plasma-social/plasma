package social.plasma.models

import com.squareup.moshi.Json
import okio.ByteString
import java.time.Instant

/** An event, but with the content being resolved into a true type */
data class TypedEvent<T>(
    val id: ByteString,
    @Json(name = "pubkey")
    val pubKey: ByteString,
    @Json(name = "created_at")
    val createdAt: Instant,
    val kind: Int,
    val tags: List<List<String>>,
    val content: T,
    val sig: ByteString,
)

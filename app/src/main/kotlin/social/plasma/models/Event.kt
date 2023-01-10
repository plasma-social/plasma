package social.plasma.models

import com.squareup.moshi.Json
import okio.ByteString
import java.time.Instant

/**
 * An event is the only kind of object that exists in nostr.
 * It can be further specialised by `kind`.
 */
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
) {
    fun maybeToNote(): Note? =
        if (kind == 1) Note(
            id = id.hex(),
            content = content,
            pubKey = pubKey.hex(),
            createdAt = createdAt
        )
        else null

}

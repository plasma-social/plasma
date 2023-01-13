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
    val tags: List<List<String>>,
    val content: String,
    val sig: ByteString,
) {

    fun <T : Any> typed(data: T) = TypedEvent(id, pubKey, createdAt, kind, tags, data, sig)

    object Kind {
        const val MetaData = 0
        const val Note = 1
        const val RecommendServer = 2
        const val ContactList = 3
    }
}

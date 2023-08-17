package social.plasma.models

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import fr.acinq.secp256k1.Secp256k1
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
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
        const val Audio: Int = 1808
        const val Repost: Int = 6
        const val MetaData = 0
        const val Note = 1
        const val RecommendServer = 2
        const val ContactList = 3
        const val Reaction = 7
    }

    companion object {

        private val moshi = Moshi.Builder().build()

        fun createEvent(
            pubKey: ByteString,
            secretKey: ByteString,
            createdAt: Instant,
            kind: Int,
            tags: List<List<String>>,
            content: String,
        ): Event {
            val elements = listOf(
                0,
                pubKey.hex(),
                createdAt.epochSecond,
                kind,
                tags,
                content,
            )
            val json = moshi.adapter(List::class.java).toJson(elements)
            val id = json.encodeUtf8().sha256()
            val sig = Secp256k1.signSchnorr(id.toByteArray(), secretKey.toByteArray(), null)
            return Event(
                id = id,
                pubKey = pubKey,
                createdAt = createdAt,
                kind = kind,
                tags = tags,
                content = content,
                sig = sig.toByteString(),
            )
        }
    }
}

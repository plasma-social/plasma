package social.plasma.nostr.relay.message

import com.squareup.moshi.Json
import social.plasma.models.Event
import java.time.Instant
import kotlin.time.Duration.Companion.hours

data class Filter(
    val ids: Set<String>? = null,
    val since: Instant? = null,
    val authors: Set<String>? = null,
    val kinds: Set<Int>? = null,
    @Json(name = "#e")
    val eTags: Set<String>? = null,
    @Json(name = "#p")
    val pTags: Set<String>? = null,
    @Json(name = "#t")
    val hashTags: Set<String>? = null,
    val limit: Int? = null,
) {
    companion object {

        val globalFeedNotes = Filter(
            since = Instant.now().minusSeconds(12.hours.inWholeSeconds),
            kinds = setOf(Event.Kind.Note),
            limit = 500,
        )

        fun userNotes(pubKey: String, since: Instant = Instant.EPOCH) = userNotes(
            pubKeys = setOf(pubKey),
            since = since,
        )

        fun userNotes(pubKeys: Set<String>, since: Instant = Instant.EPOCH) = Filter(
            since = since,
            authors = pubKeys,
            kinds = setOf(Event.Kind.Note, Event.Kind.Repost),
            limit = 500,
        )

        fun contactList(pubKey: String) = Filter(
            since = Instant.EPOCH,
            authors = setOf(pubKey),
            kinds = setOf(Event.Kind.ContactList),
            limit = 1,
        )

        fun userMetaData(pubKey: String) = userMetaData(pubKeys = setOf(pubKey))

        fun userMetaData(pubKeys: Set<String>) = Filter(
            authors = pubKeys,
            kinds = setOf(Event.Kind.MetaData),
        )

        fun noteReactions(id: String): Filter = Filter(
            since = Instant.EPOCH,
            kinds = setOf(Event.Kind.Reaction),
            eTags = setOf(id),
        )

        fun userFollowers(pubKey: String) = Filter(
            kinds = setOf(Event.Kind.ContactList),
            pTags = setOf(pubKey),
        )

    }
}

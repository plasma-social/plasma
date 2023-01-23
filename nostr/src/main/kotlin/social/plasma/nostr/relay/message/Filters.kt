package social.plasma.nostr.relay.message

import com.squareup.moshi.Json
import social.plasma.nostr.models.Event
import java.time.Instant
import kotlin.time.Duration.Companion.hours

data class Filters(
    val since: Instant = Instant.now(),
    val authors: Set<String> = emptySet(),
    val kinds: Set<Int> = emptySet(),
    @Json(name = "#e")
    val eTags: Set<String> = emptySet(),
    val limit: Int? = null,
) {
    companion object {

        val globalFeedNotes = Filters(
            since = Instant.now().minusSeconds(12.hours.inWholeSeconds),
            kinds = setOf(Event.Kind.Note),
            limit = 500,
        )

        fun userNotes(pubKey: String, since: Instant = Instant.EPOCH) = userNotes(
            pubKeys = setOf(pubKey),
            since = since,
        )

        fun userNotes(pubKeys: Set<String>, since: Instant = Instant.EPOCH) = Filters(
            since = since,
            authors = pubKeys,
            kinds = setOf(Event.Kind.Note),
        )

        fun contactList(pubKey: String) = Filters(
            since = Instant.EPOCH,
            authors = setOf(pubKey),
            kinds = setOf(Event.Kind.ContactList),
            limit = 1,
        )

        fun userMetaData(pubKey: String) = Filters(
            since = Instant.EPOCH,
            authors = setOf(pubKey),
            kinds = setOf(Event.Kind.MetaData),
            limit = 1,
        )

        fun noteReactions(id: String): Filters = Filters(
            since = Instant.EPOCH,
            kinds = setOf(Event.Kind.Reaction),
            eTags = setOf(id),
        )

    }
}

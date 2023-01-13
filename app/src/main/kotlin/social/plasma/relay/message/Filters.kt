package social.plasma.relay.message

import social.plasma.models.Event
import java.time.Instant

data class Filters(
    val since: Instant = Instant.now(),
    val authors: Set<String> = emptySet(),
    val kinds: Set<Int> = emptySet(),
) {
    companion object {

        val globalFeedNotes = Filters(
            since = Instant.now().minusSeconds(600),
            kinds = setOf(Event.Kind.Note)
        )

        fun contactList(pubKey: String) = Filters(
            since = Instant.EPOCH,
            authors = setOf(pubKey),
            kinds = setOf(Event.Kind.ContactList),
        )

        fun userMetaData(pubKey: String) = Filters(
            since = Instant.EPOCH,
            authors = setOf(pubKey),
            kinds = setOf(Event.Kind.MetaData),
        )

    }
}

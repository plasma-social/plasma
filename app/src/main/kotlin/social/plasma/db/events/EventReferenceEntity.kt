package social.plasma.db.events

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Defines a reference between two events.
 * https://github.com/nostr-protocol/nips/blob/master/10.md
 *
 * @property sourceEvent is the id of the event making the reference
 * @property targetEvent is the id of of the event being referenced.
 * @property relayUrl is the URL of a recommended relay associated with the reference
 * @property marker is optional and if present is one of "reply", "root", or "mention".
 */
@Entity(tableName = "event_ref", primaryKeys = ["source_event", "target_event"])
data class EventReferenceEntity(
    @ColumnInfo("source_event")
    val sourceEvent: String,
    @ColumnInfo("target_event")
    val targetEvent: String,
    @ColumnInfo("relay_url")
    val relayUrl: String?,
    @ColumnInfo("marker")
    val marker: EventMarker?,
) {
    enum class EventMarker {
        Reply, Root, Mention
    }
}



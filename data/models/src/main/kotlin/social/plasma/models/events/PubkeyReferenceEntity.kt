package social.plasma.models.events

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 *  Defines a reference between an event and a pubkey.
 *
 *  @property sourceEvent the id of the event making the reference
 *  @property pubkey the pubkey being referenced
 *  @property relayUrl recommended relay for the pubkey
 */
@Entity(
    tableName = "pubkey_ref",
    primaryKeys = ["source_event", "pubkey"],
    indices = [Index("source_event"), Index("pubkey")]
)
data class PubkeyReferenceEntity(
    @ColumnInfo("source_event")
    val sourceEvent: String,
    @ColumnInfo("pubkey")
    val pubkey: String,
    @ColumnInfo("relay_url")
    val relayUrl: String?,
)

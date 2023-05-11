package social.plasma.models.events

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Defines a reference between an event and a hashtag.
 *
 * @property sourceEvent is the id of the event making the reference
 * @property hashtag the hashtag being referenced.
 */
@Entity(
    tableName = "hashtag_ref",
    primaryKeys = ["source_event", "hashtag"],
    indices = [Index("hashtag"), Index("hashtag", "source_event", unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["source_event"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HashTagReferenceEntity(
    @ColumnInfo("source_event")
    val sourceEvent: String,
    @ColumnInfo("hashtag")
    val hashtag: String,
    @ColumnInfo("pubkey")
    val pubkey: String,
)



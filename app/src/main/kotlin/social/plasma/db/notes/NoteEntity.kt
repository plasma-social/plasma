package social.plasma.db.notes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [
        Index(
            "created_at",
            "id",
            "is_reply",
            orders = [Index.Order.DESC, Index.Order.ASC, Index.Order.ASC]
        ),
        Index("created_at", "id", orders = [Index.Order.DESC, Index.Order.ASC]),
        Index("created_at", "pubkey", orders = [Index.Order.DESC, Index.Order.ASC]),
        Index(
            "created_at",
            "pubkey",
            "is_reply",
            orders = [Index.Order.DESC, Index.Order.ASC, Index.Order.ASC]
        ),
    ]
)
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val pubkey: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    val content: String,
    val sig: String,
    val source: NoteSource,
    // This should change when we start storing tags
    @ColumnInfo("is_reply")
    val isReply: Boolean,
    val tags: List<List<String>>,
)

enum class NoteSource {
    // Notes inserted from the global feed
    Global,

    // Notes inserted from a users profile
    Profile,

    // Notes inserted from the user's contact list
    Contacts,

    // Notes inserted from a thread
    Thread,

    // Notes inserted from mentions
    Notifications,

    // Notes & Replies created by the client
    Posting
}

package social.plasma.db.notes

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView(
    "SELECT events.id, events.pubkey, events.content, events.created_at, events.tags, " +
            "COUNT(reactions.id) AS reactionCount, " +
            "EXISTS( SELECT 1 from event_ref WHERE source_event = events.id) as is_reply " +
            "FROM events " +
            "LEFT JOIN event_ref reactions_ref on reactions_ref.target_event == events.id " +
            "LEFT JOIN events reactions on reactions_ref.source_event == reactions.id AND reactions.kind = 7 " +
            "WHERE events.kind IN (1, 6) " +
            "GROUP BY events.id " +
            "ORDER BY events.created_at DESC"
)
data class NoteView(
    val id: String,
    val content: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    val reactionCount: Int,
    val pubkey: String,
    @ColumnInfo(name = "is_reply")
    val isReply: Boolean,
    val tags: List<List<String>>,
)

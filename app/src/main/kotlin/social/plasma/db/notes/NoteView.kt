package social.plasma.db.notes

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView(
    "SELECT notes.*, COUNT(reactions.id) AS reactionCount " +
            "FROM notes " +
            "LEFT JOIN reactions on notes.id = reactions.noteId " +
            "GROUP BY notes.id " +
            "ORDER BY created_at DESC"
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

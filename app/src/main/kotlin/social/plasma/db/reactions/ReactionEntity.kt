package social.plasma.db.reactions

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reactions",
    indices = [
        Index(value = ["noteId"]),
    ]
)
data class ReactionEntity(
    @PrimaryKey
    val id: String,
    val content: String,
    val createdAt: Long,
    val noteId: String,
)

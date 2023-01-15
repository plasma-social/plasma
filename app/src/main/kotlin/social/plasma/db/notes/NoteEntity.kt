package social.plasma.db.notes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "pubkey")
    val pubKey: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    val content: String,
    val sig: String,
)

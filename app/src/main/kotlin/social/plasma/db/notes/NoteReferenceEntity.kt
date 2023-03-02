package social.plasma.db.notes

import androidx.room.Entity

@Entity(tableName = "note_ref", primaryKeys = ["sourceNote", "targetNote"])
data class NoteReferenceEntity(
    val sourceNote: String,
    val targetNote: String,
)

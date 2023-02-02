package social.plasma.db.notes

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "notes",
    indices = [
        Index("created_at", "id", orders = [Index.Order.DESC, Index.Order.ASC])
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
    Global, Profile, Contacts, Thread,
}

@Entity(tableName = "note_ref", primaryKeys = ["sourceNote", "targetNote"])
data class NoteReferenceEntity(
    val sourceNote: String,
    val targetNote: String
)

data class NoteThread(
    @Embedded val note: NoteView,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(NoteReferenceEntity::class, parentColumn = "targetNote", entityColumn = "sourceNote")
    )
    val childrenNotes: List<NoteView>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(NoteReferenceEntity::class, parentColumn = "sourceNote", entityColumn = "targetNote")
    )
    val parentNotes: List<NoteView>,
)

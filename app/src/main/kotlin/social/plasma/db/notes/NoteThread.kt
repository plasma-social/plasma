package social.plasma.db.notes

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NoteThread(
    @Embedded val note: NoteView,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            NoteReferenceEntity::class,
            parentColumn = "targetNote",
            entityColumn = "sourceNote"
        )
    )
    val childrenNotes: List<NoteView>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            NoteReferenceEntity::class,
            parentColumn = "sourceNote",
            entityColumn = "targetNote"
        )
    )
    val parentNotes: List<NoteView>,
)

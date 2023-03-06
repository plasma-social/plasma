package social.plasma.db.notes

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import social.plasma.db.events.EventReferenceEntity

data class NoteThread(
    @Embedded val note: NoteView,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            EventReferenceEntity::class,
            parentColumn = "target_event",
            entityColumn = "source_event"
        )
    )
    val childrenNotes: List<NoteView>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            EventReferenceEntity::class,
            parentColumn = "source_event",
            entityColumn = "target_event"
        )
    )
    val parentNotes: List<NoteView>,
)

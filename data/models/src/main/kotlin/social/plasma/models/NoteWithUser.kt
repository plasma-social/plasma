package social.plasma.models

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithUser(
    @Embedded
    val noteEntity: NoteView,

    @Relation(
        parentColumn = "pubkey",
        entityColumn = "pubkey"
    )
    val userMetadataEntity: UserMetadataEntity?,
)

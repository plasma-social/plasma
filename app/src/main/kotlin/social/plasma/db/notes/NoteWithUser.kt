package social.plasma.db.notes

import androidx.room.Embedded
import androidx.room.Relation
import social.plasma.db.usermetadata.UserMetadataEntity

data class NoteWithUser(
    @Embedded
    val noteEntity: NoteView,

    @Relation(
        parentColumn = "pubkey",
        entityColumn = "pubkey"
    )
    val userMetadataEntity: UserMetadataEntity?,
)

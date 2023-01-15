package social.plasma.db.notes

import androidx.room.Embedded
import androidx.room.Relation
import social.plasma.db.usermetadata.UserMetadataEntity

data class NoteWithUserEntity(
    @Embedded
    val noteEntity: NoteEntity,

    @Relation(
        parentColumn = "pubkey",
        entityColumn = "pubkey"
    )
    val userMetadataEntity: UserMetadataEntity?,
)

package social.plasma.models

import androidx.room.Embedded
import androidx.room.Relation
import social.plasma.models.events.EventEntity

data class NoteWithUser(
    @Embedded
    val noteEntity: EventEntity,

    @Relation(
        parentColumn = "pubkey",
        entityColumn = "pubkey"
    )
    val userMetadataEntity: UserMetadataEntity?,
)

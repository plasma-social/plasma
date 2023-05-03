package social.plasma.models

import androidx.room.Entity
import androidx.room.Fts4

/**
 * Entity to perform full-text search on a subset of user metadata fields.
 */
@Entity(
    tableName = "user_suggestion"
)
@Fts4(contentEntity = UserMetadataEntity::class)
data class UserMetadataFtsEntity(
    val pubkey: String,
    val name: String?,
    val displayName: String?,
    val nip05: String?,
)

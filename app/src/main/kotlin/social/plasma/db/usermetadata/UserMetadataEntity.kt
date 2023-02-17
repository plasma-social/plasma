package social.plasma.db.usermetadata

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_metadata")
data class UserMetadataEntity(
    @PrimaryKey
    val pubkey: String,
    val name: String?,
    val about: String?,
    val picture: String?,
    val displayName: String?,
    val banner: String?,
    val nip05: String?,
    val lud: String?,
    val website: String?,
    val createdAt: Long?,
)

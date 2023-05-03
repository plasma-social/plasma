package social.plasma.models.events

import androidx.room.Entity
import androidx.room.Fts4

/**
 * An entity to perform full text search on hashtags.
 */
@Entity(tableName = "hashtag_search")
@Fts4(contentEntity = HashTagEntity::class)
data class HashTagFtsEntity(
    val hashtag: String,
)

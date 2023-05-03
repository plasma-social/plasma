package social.plasma.models.events

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * An entity to enable performing full-text search on hashtags without duplication
 */
@Entity(
    tableName = "hashtag",
    indices = [
        Index("hashtag", unique = true)
    ]
)
data class HashTagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hashtag: String,
)

package social.plasma.models.events

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    indices = [
        Index(
            "created_at",
            "id",
            "kind",
            orders = [Index.Order.DESC, Index.Order.ASC, Index.Order.ASC]
        ),
        Index(
            "created_at",
            "pubkey",
            "kind",
            orders = [Index.Order.DESC, Index.Order.ASC, Index.Order.ASC]
        ),
    ]
)
data class EventEntity(
    @PrimaryKey
    val id: String,
    val pubkey: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    val kind: Int,
    val tags: List<List<String>>,
    val content: String,
    val sig: String,
)

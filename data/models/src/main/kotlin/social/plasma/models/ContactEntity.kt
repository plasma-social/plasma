package social.plasma.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [
        Index("owner"),
        Index(value = ["owner", "pubKey"], unique = true),
    ]
)
class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val owner: String,
    val pubKey: String,
    val homeRelay: String?,
    val petName: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
package social.plasma.db.contacts

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "contacts", indices = [Index("owner")])
class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val owner: String,
    val pubKey: String,
    val homeRelay: String?,
    val petName: String?
)
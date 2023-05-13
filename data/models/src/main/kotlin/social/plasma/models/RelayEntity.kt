package social.plasma.models

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "relays",
    indices = [
        Index("pubkey"),
    ],
    primaryKeys = ["url", "pubkey"]
)
class RelayEntity(
    val pubkey: String,
    val url: String,
    val read: Boolean,
    val write: Boolean,
)

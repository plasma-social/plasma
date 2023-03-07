package social.plasma.db.notes

import androidx.room.Entity

@Entity(tableName = "pubkey_ref", primaryKeys = ["sourceNote", "pubkey"])
data class PubkeyReferenceEntity(
    val sourceNote: String,
    val pubkey: String,
)

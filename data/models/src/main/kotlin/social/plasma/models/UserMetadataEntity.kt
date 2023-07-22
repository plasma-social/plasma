package social.plasma.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import app.cash.nostrino.crypto.PubKey
import okio.ByteString.Companion.decodeHex
import shortBech32

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
    @ColumnInfo(name = "lud")
    val lud06: String?,
    val lud16: String?,
    val website: String?,
    val createdAt: Long?,
) {
    @delegate:Ignore
    val userFacingName: String by lazy {
        displayName?.takeIf { it.isNotBlank() } ?: name?.takeIf { it.isNotBlank() }
        ?: PubKey(pubkey.decodeHex()).shortBech32()
    }

    @delegate:Ignore
    val tipAddress: TipAddress? by lazy {
        when {
            lud16 != null -> TipAddress.LightningAddress(lud16)
            lud06 != null -> TipAddress.Lnurl(lud06)
            else -> null
        }
    }
}

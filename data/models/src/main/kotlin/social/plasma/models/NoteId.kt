package social.plasma.models

import android.os.Parcelable
import fr.acinq.secp256k1.Hex
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import okio.ByteString.Companion.decodeHex
import social.plasma.models.crypto.Bech32.toBech32

@Parcelize
data class NoteId(
    val hex: String,
) : Parcelable {
    @IgnoredOnParcel
    val bech32 by lazy {
        hex.decodeHex().toByteArray().toBech32("note")
    }

    @IgnoredOnParcel
    val shortBech32 by lazy {
        with(bech32) {
            "${take(8)}:${takeLast(8)}"
        }
    }

    companion object {
        fun of(byteArray: ByteArray): NoteId {
            return NoteId(Hex.encode(byteArray))
        }
    }

}

package social.plasma.models

import fr.acinq.secp256k1.Hex
import okio.ByteString.Companion.decodeHex
import social.plasma.crypto.Bech32.toBech32

data class NoteId(
    val hex: String,
) {
    val bech32 by lazy {
        hex.decodeHex().toByteArray().toBech32("note")
    }

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

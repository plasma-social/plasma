package social.plasma.models

import fr.acinq.secp256k1.Hex
import okio.ByteString.Companion.decodeHex
import social.plasma.models.crypto.Bech32.toBech32

data class PubKey(
    val hex: String,
) {
    val bech32 by lazy {
        hex.decodeHex().toByteArray().toBech32("npub")
    }

    val shortBech32 by lazy {
        with(bech32.drop(5)) {
            "${take(8)}:${takeLast(8)}"
        }
    }

    companion object {
        fun of(byteArray: ByteArray): PubKey {
            return PubKey(Hex.encode(byteArray))
        }
    }
}


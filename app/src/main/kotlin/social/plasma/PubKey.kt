package social.plasma

import fr.acinq.secp256k1.Hex
import okio.ByteString.Companion.decodeHex
import social.plasma.crypto.Bech32.toNpub

data class PubKey(
    val hex: String,
) {
    val bech32 by lazy {
        hex.decodeHex().toByteArray().toNpub()
    }

    val shortBech32 by lazy {
        bech32.take(12)
    }

    companion object {
        fun of(byteArray: ByteArray): PubKey {
            return PubKey(Hex.encode(byteArray))
        }
    }
}

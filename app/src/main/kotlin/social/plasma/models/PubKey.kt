package social.plasma.models

import okio.ByteString.Companion.decodeHex
import social.plasma.crypto.Bech32.toNpub

data class PubKey(val value: String) {
    val bech32 by lazy {
        value.decodeHex().toByteArray().toNpub()
    }

    val shortBech32 by lazy {
        bech32.take(12)
    }
}

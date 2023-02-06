package social.plasma.crypto

import okio.ByteString

data class KeyPair(
    val pub: ByteString,
    val sec: ByteString
)
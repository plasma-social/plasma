package social.plasma.models.crypto

import okio.ByteString

data class KeyPair(
    val pub: ByteString,
    val sec: ByteString,
)

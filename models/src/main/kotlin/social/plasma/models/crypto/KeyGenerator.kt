package social.plasma.models.crypto

import okio.ByteString.Companion.toByteString
import java.security.SecureRandom

class KeyGenerator {

    fun generateKeyPair(): KeyPair {
        val bs = ByteArray(32)
        SecureRandom().nextBytes(bs)
        val sec = bs.toByteString()
        if (sec.hex() > MAX_SEC) {
            return generateKeyPair()
        }
        return KeyPair(
            Bech32.pubkeyCreate(bs).toByteString(),
            sec
        )
    }

    companion object {
        private const val MAX_SEC =
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141"
    }
}
